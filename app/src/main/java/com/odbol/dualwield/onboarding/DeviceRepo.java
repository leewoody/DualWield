package com.odbol.dualwield.onboarding;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import com.burns.android.ancssample.BLEservice;

import static android.content.Context.MODE_PRIVATE;

public class DeviceRepo {

    private final SharedPreferences prefs;

    public DeviceRepo(Context context) {
        this.prefs = context.getSharedPreferences("DEVICE_REPO", MODE_PRIVATE);
    }

    public void savePairedDevice(BluetoothDevice device) {
        prefs.edit()
                .putString(BLEservice.EXTRA_BT_ADDRESS, device.getAddress())
                .apply();
    }

    public String getPairedDevice() {
        return prefs.getString(BLEservice.EXTRA_BT_ADDRESS, null);
    }

}
