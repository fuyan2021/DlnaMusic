package com.zxt.dlna.dms.bean;

import java.io.Serializable;

/**
 * 作曲家信息
 */
public class ComposerInfo implements  Serializable {

	public static final long serialVersionUID = 1L;

	private Long id;//数据库ID
	private int api;//接口
	private int artistId;//歌手ID
	private String artistMid;//歌手MID
	private String name;//姓名
	private String alias;//别名
	private String nationality;//国籍
	private String constellation;//星座
	private String birthday;//生日
	private String placeOfBirth;//出生地
	private String summary;//简介
	private String experience;//经历
	private String honour;//荣誉
	/** 中文名 外文名 别名 国籍 出生地 职业 经纪公司 唱片公司 身高 体重 血型 星座 出生日期 代表作品 生肖 毕业院校 主要成就 等等.. */
	private String infos;//所有基本信息
	private int appArea;//应用地区标识

	//TADB 数据
	private String genre;//流派
	private String gender;//性别
	private String thumb;//歌手图片
	private String logo;//歌手名字图片
	private String clearArt;//歌手高清图
	private String wideThumb;//歌手缩略图片
	private String fanArt;//歌迷上传歌手写真图一
	private String fanArt2;//歌迷上传歌手写真图二
	private String fanArt3;//歌迷上传歌手写真图三
	private String banner;//横幅图

	private int sort = 0;
	private boolean exist = true;

	private String customCover;
	private String pinyinName;
	private long favorTime;
	private boolean favor;
	private int randomCoverIndex;//随机默认封面图片index
	private long updateTime;
	private boolean isVaArtist;

	public ComposerInfo() {
	}
	public Long getId() {
		return id;
	}

	public void setApi(int api) {
		this.api = api;
	}

	public int getApi() {
		return api;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		sort = 0;//标题变化，要重新排序
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getConstellation() {
		return constellation;
	}

	public void setConstellation(String constellation) {
		this.constellation = constellation;
	}

	public int getArtistId() {
		return artistId;
	}

	public void setArtistId(int artistId) {
		this.artistId = artistId;
	}

	public String getArtistMid() {
		return artistMid;
	}

	public void setArtistMid(String artistMid) {
		this.artistMid = artistMid;
	}

	public String getPlaceOfBirth() {
		return placeOfBirth;
	}

	public void setPlaceOfBirth(String placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getExperience() {
		return experience;
	}

	public void setExperience(String experience) {
		this.experience = experience;
	}

	public String getHonour() {
		return honour;
	}

	public void setHonour(String honour) {
		this.honour = honour;
	}

	public String getInfos() {
		return infos;
	}

	public void setInfos(String infos) {
		this.infos = infos;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getThumb() {
		return thumb;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getClearArt() {
		return clearArt;
	}

	public void setClearArt(String clearArt) {
		this.clearArt = clearArt;
	}

	public String getWideThumb() {
		return wideThumb;
	}

	public void setWideThumb(String wideThumb) {
		this.wideThumb = wideThumb;
	}

	public String getFanArt() {
		return fanArt;
	}

	public void setFanArt(String fanArt) {
		this.fanArt = fanArt;
	}

	public String getFanArt2() {
		return fanArt2;
	}

	public void setFanArt2(String fanArt2) {
		this.fanArt2 = fanArt2;
	}

	public String getFanArt3() {
		return fanArt3;
	}

	public void setFanArt3(String fanArt3) {
		this.fanArt3 = fanArt3;
	}

	public String getBanner() {
		return banner;
	}

	public void setBanner(String banner) {
		this.banner = banner;
	}

	public int getAppArea() {
		return this.appArea;
	}

	public void setAppArea(int appArea) {
		this.appArea = appArea;
	}

	public boolean getExist() {
		return this.exist;
	}

	public void setExist(boolean exist) {
		this.exist = exist;
	}

	public int getSort() {
		return this.sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String getCustomCover() {
		return this.customCover;
	}

	public void setCustomCover(String customCover) {
		this.customCover = customCover;
	}

	public String getPinyinName() {
		return this.pinyinName;
	}

	public void setPinyinName(String pinyinName) {
		this.pinyinName = pinyinName;
	}

	public boolean getFavor() {
		return this.favor;
	}

	public void setFavor(boolean favor) {
		this.favor = favor;
	}

	public int getRandomCoverIndex() {
		return this.randomCoverIndex;
	}

	public void setRandomCoverIndex(int randomCoverIndex) {
		this.randomCoverIndex = randomCoverIndex;
	}

	public long getFavorTime() {
		return this.favorTime;
	}

	public void setFavorTime(long favorTime) {
		this.favorTime = favorTime;
	}

	public long getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isVaArtist() {
		return isVaArtist;
	}

	public void setVaArtist(boolean vaArtist) {
		isVaArtist = vaArtist;
	}

	public boolean getIsVaArtist() {
		return this.isVaArtist;
	}

	public void setIsVaArtist(boolean isVaArtist) {
		this.isVaArtist = isVaArtist;
	}
}
