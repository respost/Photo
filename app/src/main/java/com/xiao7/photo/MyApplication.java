package com.xiao7.photo;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
/**
 * 自定义全局Application
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 加载系统默认设置，字体不随用户设置变化
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}