package com.burns.android.ancssample;


import com.burns.android.ancssample.ANCSGattCallback.StateListener;
import com.burns.android.ancssample.icons.IosIconRepo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BLEservice extends Service implements ANCSParser.onIOSNotification
		, ANCSGattCallback.StateListener{

	public static final String EXTRA_BT_ADDRESS = "addr";
	public static final String EXTRA_IS_AUTO_CONNECT = "auto";

	private static final String TAG="BLEservice";
	private static final String CHANNEL_ONGOING = "CHANNEL_ONGOING";

	private static final int IOS_NOTIFS_OFFSET = 100;

	private final IBinder mBinder = new MyBinder();
    private NotificationManager notificationManager;
    private ANCSParser mANCSHandler;
	private ANCSGattCallback mANCScb;
	BluetoothGatt mBluetoothGatt;
	BroadcastReceiver mBtOnOffReceiver;
	boolean mAuto;
	String addr;
	int mBleANCS_state = 0;
    private final List<String> notificationChannels = new ArrayList<>();

	private IosIconRepo iconRepo;
	private NotificationDeleter deleter;

	public class MyBinder extends Binder {
    	BLEservice getService() {
            // Return this instance  so clients can call public methods
            return BLEservice.this;
        }
    }
    @SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg){
			switch (msg.what) {
			case 11:	//bt off, stopSelf()
				stopSelf();
				startActivityMsg();
				break;
			}
    	}
    };
    // when bt off,  show a Message to notify user that ble need re_connect
    private void startActivityMsg(){
    	// TODO(tyler): post notification instead.
    	Intent i = new Intent(this,Notice.class);
    	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(i);
    }
    
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");

		iconRepo = new IosIconRepo(this);

        notificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
		createOngoingNotificationChannel();

		mANCSHandler = ANCSParser.getDefault(this);
		mANCSHandler.listenIOSNotification(this);

		deleter = new NotificationDeleter(mANCSHandler);
		deleter.register(this);

		mANCScb = new ANCSGattCallback(this, mANCSHandler);
		mBtOnOffReceiver = new BroadcastReceiver() {
			public void onReceive(Context arg0, Intent i) {
				// action must be bt on/off .
				int state = i.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						BluetoothAdapter.ERROR);
				if (state == BluetoothAdapter.STATE_OFF) {
					Log.i(TAG,"bluetooth OFF !");
					mHandler.sendEmptyMessageDelayed(11, 500);
				}
			}
		};
		IntentFilter filter= new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// bt on/off
		registerReceiver(mBtOnOffReceiver, filter);
		Log.i(TAG,"onCreate()");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			mAuto = intent.getBooleanExtra(BLEservice.EXTRA_IS_AUTO_CONNECT, true);
			addr = intent.getStringExtra(BLEservice.EXTRA_BT_ADDRESS);

			if (mAuto) {
				startBleConnect(addr, mAuto);
			}
		}
		Log.i(TAG,"onStartCommand() flags="+flags+",stardId="+startId);
		return START_STICKY_COMPATIBILITY;
		//return startId;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG," onDestroy()");
		deleter.unregister();
		mANCScb.stop();
		mANCSHandler.removeListenerIOSNotification(this);
		unregisterReceiver(mBtOnOffReceiver);
		Editor e =getSharedPreferences(MainActivity.PREFS_NAME, 0).edit();
		e.putInt(MainActivity.BleStateKey, ANCSGattCallback.BleDisconnect);
		e.commit();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent i) {
		Log.i(TAG," onBind()thread id ="+android.os.Process.myTid());
		return mBinder;
	}

	//** when ios notification changed
	@Override
	public void onIOSNotificationAdd(IOSNotification noti) {
		final String text;
		if (TextUtils.isEmpty(noti.subtitle)) {
			text = noti.message;
		} else {
			text = noti.subtitle + " \n" + noti.message;
		}

		NotificationCompat.Builder build = new NotificationCompat.Builder(this, getChannel(noti))
            .setSmallIcon(iconRepo.getResourceIdForCategoryIcon(noti))
            .setContentTitle(noti.title)
            .setContentText(text)
			.setAutoCancel(false)
			.setDeleteIntent(deleter.createDeleteIntent(noti));
		notificationManager.notify(noti.uid + IOS_NOTIFS_OFFSET, build.build());
	}

	private String getChannel(IOSNotification noti) {
        // TODO: make a channel per iPhone app id.
        String channelId = "allappspackage";

        if (!notificationChannels.contains(channelId)) {
            createNotificationChannel(channelId, "All Apps");
            notificationChannels.add(channelId);
        }
        return channelId;
    }

    private void createNotificationChannel(String channelId, String name) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = getString(R.string.channel_description, name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
	public void onIOSNotificationRemove(int uid) {
		notificationManager.cancel(uid + IOS_NOTIFS_OFFSET);
	}
	
	//** public method , for client to call
	public void startBleConnect(String addr, boolean auto) {
		Log.i(TAG,"startBleConnect auto: " + auto);
		if (mBleANCS_state != 0) {
			Log.i(TAG,"stop ancs,then restart it");
			mANCScb.stop();
		}
		mAuto = auto;
		this.addr = addr;
		BluetoothDevice dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);
		mBluetoothGatt = dev.connectGatt(this, auto, mANCScb, BluetoothDevice.TRANSPORT_LE);
		mANCScb.setBluetoothGatt(mBluetoothGatt);
		mANCScb.setStateStart();

		postOngoing();
	}

	public void registerStateChanged(StateListener sl) {
		Log.i(TAG,"registerStateChanged");
		if (null != sl)
			mANCScb.addStateListen(sl);
		mANCScb.addStateListen(this);
	}
	public void connect(){
		if (!mAuto)
			mBluetoothGatt.connect();
	}
	
	public String getStateDes(){
		return mANCScb.getState();
	}
	
	public int getmBleANCS_state() {
		return mBleANCS_state;
	}

	@Override
	public void onStateChanged(int state) {
		Log.d(TAG, "onStateChanged " + state);
		mBleANCS_state = state;
		postOngoing();
	}


	private void postOngoing() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent =
				PendingIntent.getActivity(this, 0, notificationIntent, 0);

		boolean isEnabled = mBleANCS_state != ANCSGattCallback.BleDisconnect;

		Notification notification =
				new Notification.Builder(this, CHANNEL_ONGOING)
						.setContentTitle(getText(isEnabled ? R.string.ongoing_notification_title_enabled : R.string.ongoing_notification_title_disabled))
						.setContentText(getText(isEnabled ? R.string.ongoing_notification_message_connected : R.string.ongoing_notification_message_disconnected))
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentIntent(pendingIntent)
						.setPriority(Notification.PRIORITY_LOW)
						.build();

		startForeground(IOS_NOTIFS_OFFSET - 1, notification);
	}

	private void createOngoingNotificationChannel() {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String description = getString(R.string.ongoing_notification_channel_description);
			String name = getString(R.string.app_name);
			int importance = NotificationManager.IMPORTANCE_LOW;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ONGOING, name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			notificationManager.createNotificationChannel(channel);
		}
	}
}
