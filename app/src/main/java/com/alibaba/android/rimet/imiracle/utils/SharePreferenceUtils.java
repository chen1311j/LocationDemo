package com.alibaba.android.rimet.imiracle.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.alibaba.fastjson.JSON;

public class SharePreferenceUtils {

    private static final String FILE_NAME = "security_app_cache";

    public static String getLoginIdentity(Context context){
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString("identity", "");
    }

    public static void saveLoginIdentity(Context context, String identity){
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString("identity", identity).apply();
    }

    public static <T> void putByClass(Context context, String key, T entity) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, entity == null? "" : JSON.toJSONString(entity));
        editor.apply();
    }

    public static <T> T getByClass(Context context, String key, Class<T> clazz) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(key, "");
        if (TextUtils.isEmpty(json)) return null;
        return JSON.parseObject(json, clazz);
    }
}
