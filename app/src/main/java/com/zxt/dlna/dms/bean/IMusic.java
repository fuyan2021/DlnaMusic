package com.zxt.dlna.dms.bean;


import android.support.annotation.NonNull;

import java.io.File;
import java.io.Serializable;

/**
 * 音乐接口
 * Created by LIC on 2018-04-13.
 */
public abstract class IMusic implements Serializable {

	public int errorNum = 0;

	/**
	 * 获取音乐的唯一标识
	 *
	 * @return 唯一标识
	 */
	public abstract long getMusicId();

	/**
	 * 获取音乐类型
	 *
	 * @return 音乐类型
	 */
	public abstract MusicType getType();

	/**
	 * 获取音乐的绝对路径，用于播放
	 *
	 * @return 如果没有加载过.返回null；如果已经加载过，返回文件的绝对路径，这个路径有可能失效
	 */
	public abstract String getPath();

	/**
	 * 获取音乐的标题
	 *
	 * @return 标题
	 */
	public abstract String getTitle();

	/**
	 * 获取艺术家
	 *
	 * @return 艺术家
	 */
	public abstract String getArtist();

	public abstract String getAlbum();

	/**
	 * 获取用于显示的扩展名
	 *
	 * @return 扩展名
	 */
	@NonNull
	public abstract String getDisplayExtension();



	/**
	 * 是否是分轨专辑歌曲
	 */
	public boolean isList() {
		return false;
	}

	/**
	 * 是否是CUE分轨歌曲
	 */
	public boolean isCue() {
		return false;
	}

	/**
	 * 是否是DSF分轨歌曲
	 */
	public boolean isDsf() {
		return false;
	}

	public boolean isSameAs(IMusic other) {
		return other != null && other.getType() == getType() && other.getMusicId() == getMusicId();
	}

	public long getStartPosition() {
		return -1;
	}

	public long getEndPosition() {
		return -1;
	}

	public int getNumber() {
		return -1;
	}


	public boolean getIsMQA() {
		return false;
	}

	public int getMqaMode() {
		return 0;
	}

	public int getMQAUiState() {
		return -1;
	}

	public int getDuration() {
		return 0;
	}

	public int getChannels() {
		return 0;
	}

	public int getMqaOutSampleRate() {
		return -1;
	}

	public long getFileSize(){
		return 0;
	}

	public File getFile(){
		return null;
	}

	public boolean isLocalCueSong(){
		return false;
	}

	public String getMQAReplayGain() {
		return "";
	}

	public String getReplayGainAlbum() {
		return "";
	}
}
