package com.zxt.dlna.dms;

import static com.zxt.dlna.dms.EversoloLibraryService.CLING_SETTING_NAME;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

/**
 * 广播接收器，用于接收其他应用发送的广播来控制服务的开启、关闭和数据刷新
 */
public class EversoloLibraryReceiver extends BroadcastReceiver {
    private static final String TAG = "EversoloLibraryReceiver";
    
    // 广播动作常量
    public static final String ACTION_START_SERVER = "com.zxt.dlna.ACTION_START_SERVER";
    public static final String ACTION_STOP_SERVER = "com.zxt.dlna.ACTION_STOP_SERVER";
    public static final String ACTION_REFRESH_DATA = "com.zxt.dlna.ACTION_REFRESH_DATA";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || context == null) {
            return;
        }
        int status = 0;
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);
        status = Settings.System.getInt(context.getContentResolver(),CLING_SETTING_NAME,0);
        Intent serviceIntent = new Intent(context, EversoloLibraryService.class);
        
        switch (action) {
            case ACTION_START_SERVER:
                // 启动服务

                context.startService(serviceIntent);
                Log.d(TAG, "Service started");
                break;
            
            case ACTION_STOP_SERVER:
                // 停止服务
                context.stopService(serviceIntent);
                Log.d(TAG, "Service stopped");
                break;
            
            case ACTION_REFRESH_DATA:
                // 刷新数据
                // 先启动服务（如果未启动）并传递刷新数据标志
                serviceIntent.putExtra("REFRESH_DATA", true);
                context.startService(serviceIntent);
                Log.d(TAG, "Data refresh requested");
                break;
        }
    }
}