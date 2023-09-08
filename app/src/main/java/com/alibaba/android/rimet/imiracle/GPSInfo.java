package com.alibaba.android.rimet.imiracle;

public class GPSInfo {

    private String provider;//定位方式
    private String MaxSatellites;//搜索的的卫星数量

    private double longitude;//经度
    private double latitude;//纬度
    private boolean isExpired;//定位是否过期

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getMaxSatellites() {
        return MaxSatellites;
    }

    public void setMaxSatellites(String maxSatellites) {
        MaxSatellites = maxSatellites;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.isExpired =false;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.isExpired = false;
        this.latitude = latitude;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    @Override
    public String toString() {
        return "GPSInfo{" +
                "provider='" + provider + '\'' +
                ", MaxSatellites=" + MaxSatellites +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }

    public String getLocaionInfo(){
        return "(System)\nlongitude = "+longitude+"\n\nlatitude = "+latitude;
    }

    public boolean isAvailable(){
        return !isExpired && (latitude != 0 || longitude != 0);
    }
}
