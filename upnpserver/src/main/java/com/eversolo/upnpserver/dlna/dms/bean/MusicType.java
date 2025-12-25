package com.eversolo.upnpserver.dlna.dms.bean;

import java.io.Serializable;

/**
 * 音乐类型
 * Created by LIC on 2018-07-27.
 */
public enum MusicType implements Serializable {
	MEDIA,//安卓媒体库音乐
	AUDIO,//扫描的音乐
	LOCAL,//第三方应用打开音乐播放器自动生成的本地音乐
	WEB,//第三方应用打开音乐播放器自动生成的网络音乐
	CD//第三方应用打开音乐播放器自动生成的网络音乐
}
