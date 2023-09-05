package com.alibaba.android.rimet.imiracle

class Gps {
    var latitude = 0.0
    var longitude = 0.0

    constructor()
    constructor(mLongitude: Double, mLatitude: Double) {
        latitude = mLatitude
        longitude = mLongitude
    }

    override fun toString(): String {
        return "$longitude,$latitude"
    }
}