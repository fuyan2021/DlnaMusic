package com.eversolo.upnpserver.dlna.dms.bean;

import java.util.List;

/**
 * 原盘镜像文件歌曲列表信息
 */
public class CueList {

	private String performer = null; // 歌手
	private String albumName = null; // 专辑名
	private String fileName = null; // 对应的音频文件名
	private List<CueInfo> songs = null; // 歌曲列表

	public String getPerformer() {
		return performer;
	}

	public void setPerformer(String performer) {
		this.performer = performer;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<CueInfo> getSongs() {
		return songs;
	}

	public void setSongs(List<CueInfo> songs) {
		this.songs = songs;
	}

	@Override
	public String toString() {
		return "CueFileBean [performer=" + performer + ", albumName=" + albumName + ", fileName=" + fileName + ", songs=" + songs + "]";
	}

}