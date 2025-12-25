package com.eversolo.upnpserver.dlna.dms.bean;

/**
 * 原盘镜像中的歌曲信息
 */
public class CueInfo {

	private String title;// 歌名
	private String performer;// 歌手
	private String indexBegin;// 开始位置
	private String indexEnd;// 结束位置

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPerformer() {
		return performer;
	}

	public void setPerformer(String performer) {
		this.performer = performer;
	}

	public String getIndexBegin() {
		return indexBegin;
	}

	public void setIndexBegin(String indexBegin) {
		this.indexBegin = indexBegin;
	}

	public String getIndexEnd() {
		return indexEnd;
	}

	public void setIndexEnd(String indexEnd) {
		this.indexEnd = indexEnd;
	}

	@Override
	public String toString() {
		return "CueInfo{" +
			"title='" + title + '\'' +
			", performer='" + performer + '\'' +
			", indexBegin='" + indexBegin + '\'' +
			", indexEnd='" + indexEnd + '\'' +
			'}';
	}
}
