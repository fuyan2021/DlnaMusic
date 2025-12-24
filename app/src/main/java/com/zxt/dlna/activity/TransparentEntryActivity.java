package com.zxt.dlna.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.zxt.dlna.dms.EversoloLibraryService;

/**
 * 透明Activity作为应用入口点
 * 启动后立即启动服务并关闭自己
 */
public class TransparentEntryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        // 启动DLNA服务
//        startService(new Intent(this, EversoloLibraryService.class));
//
        // 立即关闭Activity，用户看不到界面
        finish();
    }
}