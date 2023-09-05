package com.alibaba.android.rimet.imiracle;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.alibaba.android.rimet.imiracle.utils.SharePreferenceUtils;

import java.util.Iterator;

public class LocationService extends Service {

    public static String LOCATION_CACHE_KEY = "location_cache_key";

    private LocationManager manager;
    private GpsStatus.Listener listener;

    @Override
    public void onCreate() {
        super.onCreate();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 状态监听
        listener = event -> {
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i("tag", "第一次定位");
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i("tag", "卫星状态改变");
                    // 获取当前状态
                    GpsStatus gpsStatus = manager.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        count++;
                    }
                    GPSInfo gpsInfo = SharePreferenceUtils.getByClass(this, LOCATION_CACHE_KEY, GPSInfo.class);
                    if(gpsInfo == null) gpsInfo = new GPSInfo();
                    gpsInfo.setMaxSatellites(count);
                    SharePreferenceUtils.putByClass(this, LOCATION_CACHE_KEY, gpsInfo);
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i("tag", "定位启动");
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i("tag", "定位结束");
                    break;
            }
        };
        manager.addGpsStatusListener(listener);
        String bestProvider = manager.getBestProvider(getCriteria(), true);
        Log.i("tag", "beseProvider = ====>" + bestProvider);
        Location lastKnownLocation = manager.getLastKnownLocation(bestProvider);
        if(lastKnownLocation != null){
            GPSInfo gpsInfo = SharePreferenceUtils.getByClass(this, LOCATION_CACHE_KEY, GPSInfo.class);
            if(gpsInfo == null) gpsInfo = new GPSInfo();
            gpsInfo.setProvider(bestProvider);
            gpsInfo.setLatitude(lastKnownLocation.getLatitude());
            gpsInfo.setLongitude(lastKnownLocation.getLongitude());
            return;
        }

        manager.requestLocationUpdates(bestProvider, 0, 0, location -> {
            Log.i("tag", "location changed====>"+location);
            if(location == null) return;
            GPSInfo gpsInfo = SharePreferenceUtils.getByClass(this, LOCATION_CACHE_KEY, GPSInfo.class);
            if(gpsInfo == null) gpsInfo = new GPSInfo();
            gpsInfo.setProvider(bestProvider);
            gpsInfo.setLatitude(location.getLatitude());
            gpsInfo.setLongitude(location.getLongitude());
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Criteria getCriteria(){
        Criteria criteria= new  Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        //  设置是否允许运营商收费
        criteria.setCostAllowed(true);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        //  设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return  criteria;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(manager != null && listener != null){
            manager.removeGpsStatusListener(listener);
        }
    }
}
