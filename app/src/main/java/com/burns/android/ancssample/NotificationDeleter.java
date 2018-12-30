package com.burns.android.ancssample;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class NotificationDeleter extends BroadcastReceiver {

    private static final String ACTION_NOTIFICATION_DELETED = "com.odbol.dualwield.ACTION_NOTIFICATION_DELETED";
    private static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";

    private final ANCSParser ancs;
    private Context context;

    public NotificationDeleter(ANCSParser parser) {
        this.ancs = parser;
    }

    public void register(Context context) {
        this.context = context;
        context.registerReceiver(this, new IntentFilter(ACTION_NOTIFICATION_DELETED));
    }

    public void unregister() {
        context.unregisterReceiver(this);
    }

    public PendingIntent createDeleteIntent(IOSNotification noti) {
        int notificationId = noti.uid;
        Intent intent = new Intent(ACTION_NOTIFICATION_DELETED);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        return PendingIntent.getBroadcast(context, notificationId, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
        if (notificationId != 0) {
            ancs.clearNotification(notificationId);
        }
    }
}
