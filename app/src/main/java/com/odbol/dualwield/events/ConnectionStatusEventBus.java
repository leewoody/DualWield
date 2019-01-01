package com.odbol.dualwield.events;

import com.burns.android.ancssample.ANCSGattCallback;
import com.odbol.sensorizer.eventbus.EventBus;

public class ConnectionStatusEventBus extends EventBus<ConnectionStatusEvent> {

    private static ConnectionStatusEventBus instance;

    public synchronized static ConnectionStatusEventBus getInstance() {
        if (instance == null) {
            instance = new ConnectionStatusEventBus(true);
            instance.send(new ConnectionStatusEvent(ANCSGattCallback.BleDisconnect, false));
        }

        return instance;
    }

    protected ConnectionStatusEventBus(boolean cacheLastValue) {
        super(cacheLastValue);
    }
}
