package com.burns.android.ancssample;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class DevicesActivity extends ListActivity {

	public static final String TAG = "ANSI_BLE";
	
	public static final String PREFS_NAME = "MyPrefsFile";
	public static final String BleStateKey="ble_state";
	public static final String BleAddrKey="ble_addr";
	public static final String BleAutoKey="ble_auto_connect";
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mLEscaning = false;
	private Button mScanButton;
	private CheckBox mAutoCB;
	private List<BluetoothDevice> mList = new ArrayList<BluetoothDevice>();
	private BaseAdapter mListAdapter = new BaseAdapter() {

		@Override
		public View getView(int i, View arg1, ViewGroup arg2) {
			TextView tv = (TextView) arg1;
			if (null == tv) {
				tv = new TextView(DevicesActivity.this);
				tv.setPadding(10, 10, 10, 10);
				tv.setTextSize(20);
			}
			BluetoothDevice dev = mList.get(i);
			String name = dev.getName();
			if (TextUtils.isEmpty(name)) {
				name = dev.getAddress();
			}
			tv.setText(name);

			if (dev.getBondState() == BluetoothDevice.BOND_BONDED) {
				tv.setTextColor(Color.BLUE);
			}
			return tv;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public int getCount() {
			return mList.size();
		}
	};
	
	private LeScanCallback mLEScanCallback = new LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					boolean found = false;
					for (BluetoothDevice dev : mList) {
						if (dev.getAddress().equals(device.getAddress())) {
							Log.i(TAG,"found ble device");
							found = true;
							break;
						}
					}
					if (!found) {
						add(device);
					}
				}
			});

		}
	};
    private MidiGattServer midiServer;
	private Switch broadcastButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devices);
		mScanButton = (Button) findViewById(R.id.scan);
		broadcastButton = findViewById(R.id.broadcast);
		broadcastButton.setOnCheckedChangeListener(this::startBroadcasting);
		mAutoCB = (CheckBox) findViewById(R.id.autoconnect);
		mScanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!mLEscaning) {
					mList.clear();
					scan(true);
				} else {
					scan(false);
				}
			}
		});

		getListView().setAdapter(mListAdapter);
		initBluetooth();
	}

	private void startBroadcasting(CompoundButton compoundButton, boolean isChecked) {
		Intent service = new Intent(this, IphoneConnectionService.class);
		if (isChecked) {
			startService(service);
		} else {
			stopService(service);
		}
    }

    private void initBluetooth() {
		PackageManager pm = getPackageManager();
		boolean support = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
		if (!support) {
			Toast.makeText(this, "BLE is not supported", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		BluetoothManager mgr = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mgr.getAdapter();
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, 1);
		}
		mList.clear();
		SharedPreferences sp=this.getSharedPreferences(PREFS_NAME, 0);
		int ble_state=sp.getInt(BleStateKey, 0);
		Log.i(TAG,"read ble state : "+ble_state);
		/*
//		if(ANCSGattCallback.BleDisconnect != ble_state){
		if( ble_state > -1){ //must be 
			boolean auto = sp.getBoolean(BleAutoKey, true);
			String addr = sp.getString(BleAddrKey, "");
			Intent intent = new Intent(this,  BLEConnect.class);
			intent.putExtra(BLEservice.EXTRA_BT_ADDRESS, addr);
			intent.putExtra(BLEservice.EXTRA_IS_AUTO_CONNECT, auto);
			intent.putExtra("state", ble_state);
			startActivity(intent);
			finish();
			return;
		}
		*/
		//scan(true);
		//stop automatic scan , I try to list my iphone by mac address
		//connect is success, but status is 133, keep the code temp.
		BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("E8:8D:28:E0:AC:51");
		add(device);
	}

	private void add(BluetoothDevice device) {
		mList.add(device);
		mListAdapter.notifyDataSetChanged();
	}


	void scan(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			Log.i(TAG,"start to scan.");
			mLEscaning = true;
			mBluetoothAdapter.startLeScan(new UUID[]{
						GattConstant.Apple.sUUIDANCService,
						GattConstant.Apple.sUUIDChaNotify,
						GattConstant.Apple.sUUIDControl,
						GattConstant.Apple.sUUIDDataSource,
						GattConstant.DESCRIPTOR_UUID
					},
					mLEScanCallback
			);
			mScanButton.setText(R.string.stop_scan);

			Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
			for (BluetoothDevice bondedDevice : bondedDevices) {
				add(bondedDevice);
			}
		} else {
			if (mLEscaning) {
				mBluetoothAdapter.stopLeScan(mLEScanCallback);
				mLEscaning = false;
				mScanButton.setText(R.string.scan);
				Log.i(TAG,"stop scan");
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		scan(false);

		super.onDestroy();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.devices, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.scan:
			mList.clear();
			scan(true);
			break;
		}
		return true;
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		BluetoothDevice dev = mList.get(position);
		scan(false);
		mScanButton.postDelayed(() -> connectToDevice(dev), 300);

	}

	private void connectToDevice(BluetoothDevice dev) {
		Intent intent = new Intent(this,  BLEConnect.class);
		intent.putExtra(BLEservice.EXTRA_BT_ADDRESS, dev.getAddress());
		intent.putExtra(BLEservice.EXTRA_IS_AUTO_CONNECT, mAutoCB.isChecked());
		startActivity(intent);
		finish();
	}

}
