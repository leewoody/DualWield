package com.odbol.dualwield;

import android.util.Log;

public class Debug {
    public static final boolean DEBUG = false;

    public static void log(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }
}
