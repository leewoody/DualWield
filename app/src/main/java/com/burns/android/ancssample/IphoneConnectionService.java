package com.burns.android.ancssample;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.View;

public class IphoneConnectionService extends Service {
    private MidiGattServer midiServer;

    public IphoneConnectionService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startBroadcasting();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (midiServer != null) {
            midiServer.onDestroy();
        }
        super.onDestroy();
    }

    private void startBroadcasting() {
        if (midiServer == null) {
            midiServer = new MidiGattServer(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
