package com.alibaba.android.rimet.imiracle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.alibaba.android.rimet.imiracle.utils.BDLocationUtils;
import com.alibaba.android.rimet.imiracle.utils.GDLocationUtils;
import com.alibaba.android.rimet.imiracle.utils.SharePreferenceUtils;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;

import java.util.ArrayList;

/**
 * 定位方式：
 *    1、百度定位：BaiduLBS_Android.jar liblocSDK8b.so  总体积：800K
 *    2、高德定位：AMap_Location_V6.4.0_20230808.jar    总体积：490K
 *    3、系统GPS定位                                    总体积：0
 *
 *  特点：
 *     1、第三方（高德和百度）定位可靠，但是需要引入SDK
 *     2、系统定位国内Google服务被禁用无法进行网络定位，GPS定位可能获取不到，导致数据不可靠
 */

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_LOCATION = 0x002;
    private TextView tv_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        tv_location = findViewById(R.id.tv_location);
        if(checkPermission()){
            startLocationService();
        }

        findViewById(R.id.btn_bd).setOnClickListener(v -> getBDLocation());
        findViewById(R.id.btn_gd).setOnClickListener(view -> getGDLocation());
        findViewById(R.id.btn_system).setOnClickListener(v -> getLocation());

    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);
            Toast.makeText(this, "请允许app获取定位权限", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!isLocationEnable()){
            Toast.makeText(this, "请打开GPS定位开关", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void getBDLocation(){
        if(!checkPermission()) return;
        try {
            LocationClient.setAgreePrivacy(true);
            BDLocationUtils.getInstance().initLocationOption(new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation bdLocation) {
                    StringBuilder locationBuilder = new StringBuilder();
                    locationBuilder.append("(Baidu)\nlongitude = ").append(BDLocationUtils.parseDouble(bdLocation.getLongitude())).append(",\n\n latitude =").append(BDLocationUtils.parseDouble(bdLocation.getLatitude()));
                    locationBuilder.append("\n\n可靠度 = "+bdLocation.getMockGnssProbability());
                    tv_location.setText(locationBuilder.toString());
                    BDLocationUtils.getInstance().stopLocation();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getGDLocation(){
        if(!checkPermission()) return;
        try {
            AMapLocationClient.updatePrivacyShow(DemoApplication.getDemoApplication(), true, true);
            AMapLocationClient.updatePrivacyAgree(DemoApplication.getDemoApplication(), true);
            GDLocationUtils.getInstance().initGDMap(aMapLocation -> {
                GDLocationUtils.getInstance().stopLocation();
                if(aMapLocation == null) return;
                if (aMapLocation.getErrorCode() == 0) {
                    //解析定位结果
                    StringBuilder locationBuilder = new StringBuilder();
                    locationBuilder.append("(Gaode)\nlongitude = ").append(BDLocationUtils.parseDouble(aMapLocation.getLongitude())).append(",\n\n latitude =").append(BDLocationUtils.parseDouble(aMapLocation.getLatitude()));
                    locationBuilder.append("\n\n可靠度 = "+aMapLocation.getTrustedLevel());
                    tv_location.setText(locationBuilder.toString());
                    return;
                }
                Toast.makeText(this, aMapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        if(!checkPermission()) return;
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS是否正常启动
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            // 返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }
        GPSInfo gpsInfo = SharePreferenceUtils.getByClass(this, LocationService.LOCATION_CACHE_KEY, GPSInfo.class);
        if(gpsInfo != null && gpsInfo.isAvailable()){
            tv_location.setText(gpsInfo.getLocaionInfo());
            gpsInfo.setExpired(true);
            return;
        }
        Location lastKnownLocation = manager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
        if(lastKnownLocation == null){
            lastKnownLocation = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        if(lastKnownLocation == null){
            tv_location.setText(gpsInfo != null && gpsInfo.isAvailable()?"获取失败，过期定位："+gpsInfo.getLocaionInfo():"获取失败");
            return;
        }
        StringBuilder locationBuilder = new StringBuilder();
        locationBuilder.append("(System)\nlongitude = ").append(lastKnownLocation.getLongitude()).append(",\n\n latitude =").append(lastKnownLocation.getLatitude());
        tv_location.setText(locationBuilder.toString());
    }

    public boolean isLocationEnable(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
                return locationMode != Settings.Secure.LOCATION_MODE_OFF;
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }else{
            String locationProvider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProvider);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "定位权限已申请", Toast.LENGTH_SHORT).show();
            startLocationService();
        }
    }

    private void startLocationService(){
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }
}