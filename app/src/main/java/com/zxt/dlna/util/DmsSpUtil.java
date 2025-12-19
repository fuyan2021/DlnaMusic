package com.zxt.dlna.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by fuyan
 * 2025/12/18
 **/
public class DmsSpUtil {

    public static boolean getServerOn(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean("erversolo_server_status", true);
    }

    public static void putServerOn(Context context) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putBoolean("erversolo_server_status", true);
        editor.apply();
    }
    
    public static void setServerOn(Context context, boolean isOn) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putBoolean("erversolo_server_status", isOn);
        editor.apply();
    }

}
