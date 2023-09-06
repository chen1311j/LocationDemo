package com.alibaba.android.rimet.imiracle.utils;

import com.alibaba.android.rimet.imiracle.DemoApplication;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class GDLocationUtils {

    private static GDLocationUtils instance;
    private AMapLocationClient mLocationClient;

    public static GDLocationUtils getInstance() {
        if (instance == null) {
            synchronized (GDLocationUtils.class) {
                if (instance == null) {
                    instance = new GDLocationUtils();
                }
            }
        }
        return instance;
    }

    public void initGDMap(AMapLocationListener listener) throws Exception {
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);

        if(mLocationClient == null){
            //声明AMapLocationClient类对象
            mLocationClient = new AMapLocationClient(DemoApplication.getDemoApplication());
        }
        //设置定位回调监听
        mLocationClient.setLocationListener(listener);
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    public void stopLocation(){
        if(mLocationClient == null) return;
        mLocationClient.stopLocation();
    }
}
