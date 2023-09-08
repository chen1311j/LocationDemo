package com.alibaba.android.rimet.imiracle;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.alibaba.android.rimet.imiracle.utils.SharePreferenceUtils;

import java.util.Iterator;

public class LocationService extends Service {

    public static String LOCATION_CACHE_KEY = "location_cache_key";

    private LocationManager manager;
    private OnNmeaMessageListener listener;

    @Override
    public void onCreate() {
        super.onCreate();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

        listener = (message, timestamp) -> {
            Log.i("cj", "原始信息---->"+message);
            if(TextUtils.isEmpty(message)) return;
            if(!message.contains("GGA")) return;
            String info[] = message.split(",");
            if(info == null){
                Log.i("cj", "定位信息为空");
                return;
            }
            Log.i("cj","定位类型："+info[0]);
            Log.i("cj","UTC时间："+info[1]);

            Log.i("cj","原始纬度："+info[2]);
            double parsedLatitude = parseGPSInfo(info[2]);
            Log.i("cj","转换纬度："+parsedLatitude);
            Log.i("cj","纬度半球："+info[3]);
            Log.i("cj","原始经度："+info[4]);
            double parseLongitude = parseGPSInfo(info[4]);
            Log.i("cj","转换经度："+ parseLongitude);
            Log.i("cj","经度半球："+info[5]);
            Log.i("cj","GPS状态："+info[6]);
            Log.i("cj","使用卫星数量："+info[7]);
            Log.i("cj","HDOP-水平精度因子："+info[8]);
            Log.i("cj","椭球高："+info[9]);
            Log.i("cj","大地水准面高度异常差值："+info[10]);
            Log.i("cj","差分GPS数据期限："+info[11]);
            Log.i("cj","差分参考基站标号："+info[12]);
            Log.i("cj","ASCII码的异或校验："+info[info.length-1]);
            //UTC + (＋0800) = 本地（北京）时间
            int a= Integer.parseInt(info[1].substring(0,2));
            a+=8;
            a%=24;
            String time="";
            String time1="";
            if(a<10){
                time="0"+a+info[1].substring(2,info[1].length()-1);
            }
            else{
                time=a+info[1].substring(2,info[1].length()-1);
            }
            time1=time.substring(0,2)+":"+time.substring(2,4)+":"+time.substring(4,6);
            Log.i("cj", "北京时间："+time1);
            GPSInfo gpsInfo = SharePreferenceUtils.getByClass(this, LOCATION_CACHE_KEY, GPSInfo.class);
            if(gpsInfo == null) gpsInfo = new GPSInfo();
            gpsInfo.setMaxSatellites(info[7]);
            gpsInfo.setLatitude(parsedLatitude);
            gpsInfo.setLongitude(parseLongitude);
            SharePreferenceUtils.putByClass(this, LOCATION_CACHE_KEY, gpsInfo);
        };
        manager.addNmeaListener(listener);
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

    /**
     * 计算依据：abcde.fghi
     * @param str abc + (de/60) + (fghi)/600000
     */
    private double parseGPSInfo(String str){
        if(TextUtils.isEmpty(str)) return 0;
        try{
            double number = Double.parseDouble(str);
            return ((int)number/100)+(number%100)/60;
        }catch (NumberFormatException e){
            return 0;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(manager != null && listener != null){
            manager.removeNmeaListener(listener);
        }
    }
}
