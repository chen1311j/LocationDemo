package com.alibaba.android.rimet.imiracle;

import android.app.Application;
import android.content.Context;

public class DemoApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getDemoApplication(){
        return context;
    }
}
