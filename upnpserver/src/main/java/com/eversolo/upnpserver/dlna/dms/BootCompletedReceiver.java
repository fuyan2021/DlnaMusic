package com.eversolo.upnpserver.dlna.dms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by fuyan
 * 2025/12/25
 **/
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("upnpserver.dlna", "onReceive: 开机自启动");
            Intent serviceIntent = new Intent(context, EversoloLibraryService.class);
            context.startService(serviceIntent);
        }
    }
}
