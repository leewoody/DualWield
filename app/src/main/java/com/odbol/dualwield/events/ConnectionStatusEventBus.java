package com.odbol.dualwield.events;

import com.odbol.sensorizer.eventbus.EventBus;

public class ConnectionStatusEventBus extends EventBus<ConnectionStatusEvent> {

    private static ConnectionStatusEventBus instance;

    public synchronized static ConnectionStatusEventBus getInstance() {
        if (instance == null) {
            instance = new ConnectionStatusEventBus(true);
        }

        return instance;
    }

    protected ConnectionStatusEventBus(boolean cacheLastValue) {
        super(cacheLastValue);
    }
}
