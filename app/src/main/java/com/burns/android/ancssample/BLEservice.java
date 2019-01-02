package com.burns.android.ancssample;


import com.burns.android.ancssample.icons.IosIconRepo;
import com.odbol.dualwield.MainActivity;
import com.odbol.dualwield.events.ConnectionStatusEvent;
import com.odbol.dualwield.events.ConnectionStatusEventBus;
import com.odbol.dualwield.onboarding.DeviceRepo;

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

import io.reactivex.disposables.CompositeDisposable;

public class BLEservice extends Service implements ANCSParser.onIOSNotification {

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

	private final CompositeDisposable subscriptions = new CompositeDisposable();

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

    	}
    };
    
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
					mHandler.postDelayed(() -> mANCScb.stop(), 500);
				} else if (state == BluetoothAdapter.STATE_ON) {
					Log.i(TAG,"bluetooth ON !");
					mHandler.postDelayed(() -> startBleConnect(addr, mAuto), 500);
				}
			}
		};
		IntentFilter filter= new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// bt on/off
		registerReceiver(mBtOnOffReceiver, filter);
		Log.i(TAG,"onCreate()");

		subscriptions.add(ConnectionStatusEventBus.getInstance().subscribe().subscribe(this::onConnectionStatus));
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG,"onStartCommand() flags="+flags+",stardId="+startId);
		if (intent != null) {
			mAuto = intent.getBooleanExtra(BLEservice.EXTRA_IS_AUTO_CONNECT, true);
			addr = intent.getStringExtra(BLEservice.EXTRA_BT_ADDRESS);

			if (mAuto) {
				startBleConnect(addr, mAuto);
			}
		}
		return START_STICKY_COMPATIBILITY;
		//return startId;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG," onDestroy()");
		subscriptions.dispose();

		deleter.unregister();
		mANCScb.stop();
		mANCSHandler.removeListenerIOSNotification(this);

		ConnectionStatusEventBus.getInstance().send(new ConnectionStatusEvent(ANCSGattCallback.BleDisconnect, false));

		unregisterReceiver(mBtOnOffReceiver);
		Editor e =getSharedPreferences(DevicesActivity.PREFS_NAME, 0).edit();
		e.putInt(DevicesActivity.BleStateKey, ANCSGattCallback.BleDisconnect);
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
		if (mBleANCS_state != ANCSGattCallback.BleDisconnect) {
			Log.i(TAG,"stop ancs,then restart it");
			mBleANCS_state = ANCSGattCallback.BleDisconnect;
			mANCScb.stop();
		}
		mAuto = auto;
		if (addr == null) {
			addr = new DeviceRepo(this).getPairedDevice();
		}
		this.addr = addr;
		if (!TextUtils.isEmpty(addr)) {
			try {
				BluetoothDevice dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);
				if (dev == null) {
					throw new Exception("No device connected");
				}
				mBluetoothGatt = dev.connectGatt(this, auto, mANCScb, BluetoothDevice.TRANSPORT_LE);
				mANCScb.setBluetoothGatt(mBluetoothGatt);
				mANCScb.setStateStart();
//				mBluetoothGatt.connect();
			} catch (Exception e) {
				Log.e(TAG, "Failed to connect", e);
				mBleANCS_state = ANCSGattCallback.BleDisconnect;
			}
		}

		postOngoing();
	}

	public void connect(){
		if (!mAuto && mBluetoothGatt != null)
			mBluetoothGatt.connect();
	}
	
	public String getStateDes(){
		return mANCScb.getState();
	}
	
	public int getmBleANCS_state() {
		return mBleANCS_state;
	}

	public void onConnectionStatus(ConnectionStatusEvent event) {
    	int state = event.status;
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
						.setContentText(getText(MainActivity.getStateMessage(mBleANCS_state)))
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
