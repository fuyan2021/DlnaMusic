package com.zxt.dlna.dms.bean;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 专辑信息
 * Created by LIC on 2018-07-06.
 */
public class AlbumInfo implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	private Long id;
	private int api;
	private int albumId;
	private String albumMid;
	private String name;
	private long artistId;
	private String artist;
	private String pinyinArtist;
	private long companyId;
	private String company;//公司
	private String language;//语言
	private String pubDate;//发行时间(2004-08-03)
	private String genre;//流派
	private String summary;//摘要
	private String style;//音乐形式，比如Acoustic
	private int songCount;//歌曲数目
	private String genres;//歌曲流派集合（用,隔开）

	private String cover;//专辑封面图像
	private String backCover;//专辑背面图像
	private String cdArt;//光盘图像
	private String spine;//??图像

	private double vote;//平均分
	private int voteCount;//评分总数

	private String directoryUri;//CUE,SACD音频文件的路径或专辑文件夹的路径
	private int sort = 0;
	private boolean exist = true;
	private int position;//有的专辑有CD-1,CD-2这种

	private String customCover;
	private boolean isNumberAlbum;
	private String pinyinName;
	private long favorTime;
	private boolean favor;
	private int randomCoverIndex;//随机默认封面图片index
	private long updateTime;
	private long createTime;
	private List<AudioInfo> songs = new ArrayList<>();//专辑内的歌曲列表


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

	public int getAlbumId() {
		return this.albumId;
	}

	public void setAlbumId(int albumId) {
		this.albumId = albumId;
	}

	public String getAlbumMid() {
		return this.albumMid;
	}

	public void setAlbumMid(String albumMid) {
		this.albumMid = albumMid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
		sort = 0;//标题变化，要重新排序
	}

	public long getArtistId() {
		return this.artistId;
	}

	public void setArtistId(long artistId) {
		this.artistId = artistId;
	}

	public String getArtist() {
		return this.artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public long getCompanyId() {
		return this.companyId;
	}

	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}

	public String getCompany() {
		return this.company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getPubDate() {
		if (pubDate == null){
			return "";
		}
		return this.pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getGenre() {
		return this.genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getSummary() {
		return this.summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getBackCover() {
		return backCover;
	}

	public void setBackCover(String backCover) {
		this.backCover = backCover;
	}

	public String getCdArt() {
		return cdArt;
	}

	public void setCdArt(String cdArt) {
		this.cdArt = cdArt;
	}

	public String getSpine() {
		return spine;
	}

	public void setSpine(String spine) {
		this.spine = spine;
	}

	public double getVote() {
		return vote;
	}

	public void setVote(double vote) {
		this.vote = vote;
	}

	public int getVoteCount() {
		return voteCount;
	}

	public void setVoteCount(int voteCount) {
		this.voteCount = voteCount;
	}

	public int getSongCount() {
		return songCount;
	}

	public void setSongCount(int songCount) {
		this.songCount = songCount;
	}

	public String getDirectoryUri() {
		return this.directoryUri;
	}

	public void setDirectoryUri(String directoryUri) {
		this.directoryUri = directoryUri;
	}

	/**
	 * 是否是CUE,SACD,或专辑文件夹关联的专辑
	 */
	public boolean isList() {
		return directoryUri != null;
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

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public int getPosition() {
		return this.position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getCustomCover() {
		return this.customCover;
	}

	public void setCustomCover(String customCover) {
		this.customCover = customCover;
	}

	public boolean getIsNumberAlbum() {
		return this.isNumberAlbum;
	}

	public void setIsNumberAlbum(boolean isNumberAlbum) {
		this.isNumberAlbum = isNumberAlbum;
	}

	@Override
	public String toString() {
		return "AlbumInfo{" +
			"id=" + id +
			", api=" + api +
			", albumId=" + albumId +
			", albumMid='" + albumMid + '\'' +
			", name='" + name + '\'' +
			", artistId=" + artistId +
			", artist='" + artist + '\'' +
			", companyId=" + companyId +
			", company='" + company + '\'' +
			", language='" + language + '\'' +
			", pubDate='" + pubDate + '\'' +
			", genre='" + genre + '\'' +
			", summary='" + summary + '\'' +
			", style='" + style + '\'' +
			", songCount=" + songCount +
			", cover='" + cover + '\'' +
			", backCover='" + backCover + '\'' +
			", cdArt='" + cdArt + '\'' +
			", spine='" + spine + '\'' +
			", vote=" + vote +
			", voteCount=" + voteCount +
			", directoryUri='" + directoryUri + '\'' +
			", sort=" + sort +
			", exist=" + exist +
			", position=" + position +
			", customCover='" + customCover + '\'' +
			", isNumberAlbum=" + isNumberAlbum +
			'}';
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

	public long getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public List<AudioInfo> getSongs() {
		return this.songs;
	}

	public void setSongs(List<AudioInfo> songs) {
		this.songs = songs;
	}

	public String getPinyinArtist() {
		return this.pinyinArtist;
	}

	public void setPinyinArtist(String pinyinArtist) {
		this.pinyinArtist = pinyinArtist;
	}

	public String[] getGenresList() {
		if (TextUtils.isEmpty(genres)) {
			return null;
		} else if (genres.contains(",")) {
			return genres.split(",");
		} else {
			return new String[]{genre};
		}
	}

	public void addGenre(String genre) {
		try {
			if (TextUtils.isEmpty(genres)) {
				genres =","+ genre + ",";
			} else {
				if (genres.contains(",")) {
					if (genres.contains(","+ genre + ",")) {
						return;
					}
				} else {
					if (genres.equals(genre)) {
						return;
					}
				}
				genres = genres + genre + ",";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getGenres() {
		return genres;
	}

	public void setGenres(String genres) {
		this.genres = genres;
	}
}
