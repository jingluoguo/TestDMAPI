package com.jingluo.dm_api_module.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {

    private static class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static SharedPreferencesUtil INSTANCE = new SharedPreferencesUtil();
    }

    SharedPreferences preferences;

    public static SharedPreferencesUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final String TAG = "SharedPreferencesUtil";

    public void initSPUtil(Context context) {
        preferences=context.getSharedPreferences("local", Context.MODE_PRIVATE);
    }

    public void setString(String key, String value) {
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public String getString(String key) {
        String value = "";
        if (preferences != null) {
            value = preferences.getString(key, "");
        }
        return value;
    }


}
