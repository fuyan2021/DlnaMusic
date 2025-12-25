package com.eversolo.upnpserver.dlna.application;

import android.app.Application;
import android.content.Context;

import org.fourthline.cling.android.AndroidUpnpService;

import java.net.InetAddress;

public class BaseApplication extends Application {

	public static AndroidUpnpService upnpService;

	public static Context mContext;

	private static InetAddress inetAddress;

	private static String hostAddress;

	private static String hostName;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
	}

	public static Context getContext() {
		return mContext;
	}

	public static void setLocalIpAddress(InetAddress inetAddr) {
		inetAddress = inetAddr;

	}

	public static InetAddress getLocalIpAddress() {
		return inetAddress;
	}

	public static String getHostAddress() {
		return hostAddress;
	}

	public static void setHostAddress(String hostAddress) {
		BaseApplication.hostAddress = hostAddress;
	}

	public static String getHostName() {
		return hostName;
	}

	public static void setHostName(String hostName) {
		BaseApplication.hostName = hostName;
	}
}
