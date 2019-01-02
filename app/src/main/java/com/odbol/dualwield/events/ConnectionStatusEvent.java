package com.odbol.dualwield.events;

public class ConnectionStatusEvent {
    /***
     * One of ANCSGattCallback.BleDisconnect constants.
     */
    public final int status;

    public final boolean isServiceStarted;

    public ConnectionStatusEvent(int status, boolean isServiceStarted) {
        this.status = status;
        this.isServiceStarted = isServiceStarted;
    }

    @Override
    public String toString() {
        return "ConnectionStatusEvent status: " + status + " isServiceStarted: " + isServiceStarted;
    }
}
