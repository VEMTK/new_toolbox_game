package com.global.toolbox;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.xxm.sublibrary.services.S_service;

public class MyApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        //    Fabric.with(this, new Crashlytics());
        context = this;
        startService(new Intent(this, S_service.class));
    }

}
