package com.zxt.dlna.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;

import com.zxt.dlna.R;
import com.zxt.dlna.application.BaseApplication;
import com.zxt.dlna.dms.EversoloLibraryService;

/**
 * Created by fuyan
 * 2025/12/18
 **/
public class OpenServerActivity extends Activity {

    private static final String PREFS_NAME = "server_settings";
    private static final String KEY_SERVER_ENABLED = "server_enabled";
    
    private EversoloLibraryService libraryService;
    private boolean isServiceBound = false;
    private Switch switchCompat;
    private TextView textView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static String TAG = "23331";
    private SharedPreferences sharedPreferences;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: Service connected");
            try {
                EversoloLibraryService.LocalBinder binder = (EversoloLibraryService.LocalBinder) service;
                libraryService = binder.getService();
                isServiceBound = true;
                
                // 检查是否需要自动启动服务
                boolean isServerEnabled = sharedPreferences.getBoolean(KEY_SERVER_ENABLED, false);
                if (isServerEnabled && BaseApplication.upnpService == null) {
                    new Thread(() -> {
                        if (isServiceBound && libraryService != null) {
                            libraryService.startServer();
                            handler.post(() -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    switchCompat.setChecked(true);
                                    updateSwitchState();
                                }
                            });
                        }
                    }).start();
                }
            } catch (Exception e) {
                Log.e(TAG, "onServiceConnected: Error binding to service", e);
                isServiceBound = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: Service disconnected");
            libraryService = null;
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_server);

        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // 初始化UI组件
        switchCompat = (Switch) findViewById(R.id.switchBt);
        textView = (TextView) findViewById(R.id.text);

        // 绑定服务
        Intent intent = new Intent(this, EversoloLibraryService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        // 设置开关初始状态
        boolean isServerEnabled = sharedPreferences.getBoolean(KEY_SERVER_ENABLED, false);
        switchCompat.setChecked(isServerEnabled && BaseApplication.upnpService != null);
        updateSwitchState();

        // 设置开关监听器
        switchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            // 禁用开关，防止快速切换
            try {
                switchCompat.setEnabled(false);
                // 更新状态文本
                textView.setText(b ? "正在启动服务..." : "正在停止服务...");
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "updateSwitchState: " + e.getMessage());
            }
            if (b) {
                new Thread(() -> {
                    if (isServiceBound && libraryService != null) {
                        libraryService.startServer();
                    }
                    // 服务启动后恢复开关状态
                    handler.removeCallbacksAndMessages(null);
                    handler.post(() -> {
                        try {
                            // 检查Activity是否仍然活动
                            if (!isFinishing() && !isDestroyed()) {
                                // 保存服务状态
                                sharedPreferences.edit().putBoolean(KEY_SERVER_ENABLED, true).apply();
                                switchCompat.setEnabled(true);
                                updateSwitchState();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "updateSwitchState: " + e.getMessage());
                        }
                    });
                }).start();
            } else {
                new Thread(() -> {
                    if (isServiceBound && libraryService != null) {
                        libraryService.stopServer();
                    }
                    // 服务停止后恢复开关状态
                    handler.removeCallbacksAndMessages(null);
                    handler.post(() -> {
                        // 检查Activity是否仍然活动
                        if (!isFinishing() && !isDestroyed()) {
                            // 保存服务状态
                            sharedPreferences.edit().putBoolean(KEY_SERVER_ENABLED, false).apply();
                            switchCompat.setEnabled(true);
                            updateSwitchState();
                        }
                    });
                }).start();
            }
        });
    }

    /**
     * 更新开关状态和文本
     */
    private void updateSwitchState() {
        boolean isServerRunning = BaseApplication.upnpService != null;
        textView.setText(switchCompat.isChecked() ? "服务已启动" : "服务已停止");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源，避免内存泄漏
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        
        // 解绑服务
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        
        // 释放引用
        libraryService = null;
        switchCompat = null;
        textView = null;
        handler = null;
    }
}
