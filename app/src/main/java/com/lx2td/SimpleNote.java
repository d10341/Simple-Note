package com.lx2td;

import android.app.Application;
import android.content.Context;

public class SimpleNote extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        SimpleNote.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return SimpleNote.context;
    }
}
