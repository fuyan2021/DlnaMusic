package com.zxt.dlna.dms.bean;

import java.io.Serializable;

/**
 * 流派信息
 * Created by LIC on 2018-07-11.
 */
public class GenreInfo implements Serializable {

	public static final long serialVersionUID = 1L;

	private Long id;
	private int api;
	private int genreId;
	private long count;
	private String name;
	private String pinyinName;
	private String displayName;//编辑后显示的名字
	private String genreImage;//流派图片
	private String compareName;
	private String zhName;

	public GenreInfo() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getApi() {
		return this.api;
	}

	public void setApi(int api) {
		this.api = api;
	}

	public int getGenreId() {
		return this.genreId;
	}

	public void setGenreId(int genreId) {
		this.genreId = genreId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCount() {
		return this.count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getGenreImage() {
		return this.genreImage;
	}

	public void setGenreImage(String genreImage) {
		this.genreImage = genreImage;
	}

	public String getCompareName() {
		return this.compareName;
	}

	public void setCompareName(String compareName) {
		this.compareName = compareName;
	}

	public String getZhName() {
		return this.zhName;
	}

	public void setZhName(String zhName) {
		this.zhName = zhName;
	}

	public String getPinyinName() {
		return pinyinName;
	}

	public void setPinyinName(String pinyinName) {
		this.pinyinName = pinyinName;
	}
}
