package com.zxt.dlna.dms.bean;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 音频信息
 * Created by LIC on 2018-04-12.
 */
public class AudioInfo implements Serializable {

    public static final long serialVersionUID = 1L;


    public static final int STATE_UN_RECOGNISE = 0;//未识别
    public static final int STATE_UN_MATCH = 1;//未匹配
    public static final int STATE_FAIL = 2;//匹配失败
    public static final int STATE_SUCCESS = 3;//匹配成功
    public static final int STATE_MANUALLY = 4;//手动匹配
    public static final int STATE_CLEAR = 5;//手动清理
    public static final int STATE_DELETE = 6;//手动删除
    public static final int STATE_PART_NO_TITLE = 7;//CUE片段，且没有解析到标题，认定为匹配失败，不用进行匹配

    public static final int TYPE_SINGLE = 0;//单曲
    public static final int TYPE_CUE = 0x110;//CUE单曲
    public static final int TYPE_CUE_LIST = 0x111;//CUE列表
    public static final int TYPE_CUE_TEMP_LIST = 0x1111;//CUE列表（临时）
    public static final int TYPE_CUE_SONG = 0x102;//CUE列表歌曲
    public static final int TYPE_CUE_TEMP_SONG = 0x1102;//CUE列表临时歌曲
    public static final int TYPE_DSF = 0x200;//DSF单曲
    public static final int TYPE_DSF_LIST = 0x0211;//DSF列表
    public static final int TYPE_DSF_TEMP_LIST = 0x1211;//DSF列表（临时）
    public static final int TYPE_DSF_SONG = 0x0202;//DSF列表歌曲
    public static final int TYPE_DSF_TEMP_SONG = 0x1202;//DSF列表临时歌曲
    public static final int TYPE_UPNP_SONG = 0x1302;//DSF列表临时歌曲

    public static final int TYPE_LIST = 0x0001;
    public static final int TYPE_SONG = 0x0002;

    public static final int TAG_MEDIA = 0x0001;

    private Long id;//数据库ID
    private int type;
    private Long hashcode;//数据库ID
    private long deviceId;//所属设备数据库ID
    private long folderId = -1;//所属设备数据库ID
    private String uri; // 文件的相对路径(设备挂载路径后的路径)
    private String url;
    public String getTitle() {
        return title;
    }

    private String title;//标题（歌曲名或专辑名）
    private long albumId;//专辑ID
    private String album;//专辑名
    private String pinyinAlbum;//专辑拼音名
    private String artistIds;//艺术家ID,以' ' 隔开
    private String composerIds;//艺术家ID,以' ' 隔开
    private List<String> composerIdList;
    private String artist;//艺术家（歌手）
    private String pinyinArtist;//艺术家拼音名
    private int isVa;//是否是群星专辑歌曲
    private String albumArtist;//专辑艺术家（歌手）
    private String pinyinAlbumArtist;//专辑艺术家（歌手）
    private long infoId;//如果类型是TYPE_LIST，对应专辑信息的数据库ID,否则对应音乐信息数据库ID
    private long addedTime;//添加时间
    private long lastPlayTime = -1;//最近一次播放的时间，-1表示未播放
    private int playPoint;//当前播放时间（毫秒）
    private int duration;//音频时长（毫秒）
    private int audioType = TYPE_SINGLE;//类型
    private long listId;//如果类型为SONG，这个属性代表它所属的列表（CUE或DSF文件）在数据库的ID
    private long startPosition;//如果类型为TYPE_CUE_SONG时，表示这首歌的开始对应CUE文件的位置
    private long endPosition;//如果类型为TYPE_CUE_SONG时，表示这首歌的结束对应CUE文件的位置
    private int number;//如果类型为SONG时，表示它在列表的序号，从1开始计数
    private int state = STATE_UN_RECOGNISE;//状态
    private String sources = null;//绑定的源（音乐文件夹）
    private long modifiedTime;//文件修改时间
    private int playTimes = 0;//播放次数
    private String extension;//扩展名
    private int genre;//流派，专用于此应用对流派分类的属性
    private long genreId;//流派，专用于此应用对流派分类的属性
    private String date;//发行日期
    private String lrcFileName;//歌词文件名
    private boolean favor;//收藏
    private long favorTime;//收藏时间
    private int bitrate;//比特率
    private int SampleRate;//采样率
    private int channels;//声道
    private int channels_second;//声道2
    private int bits;//码率
    private int genreIndex;//通用流派序号
    private String genreName;//通用流派名称
    private String codec;//码流
    private int tag = 0;
    private int sort = 0;//标题排序
    private boolean exist = true;//是否存在
    private boolean isMQA = false;//是否是mqa
    private int mqaMode = 0;//是否是mqa >0 mqa
    private int mqaOutSampleRate = 0;
    private int MQAUiState = -1;

    private String recordingMid; //MusicBrainz recording ID
    private String artistMid; //MusicBrainz artist ID
    private String releaseMid; //MusicBrainz release ID

    private int isCompleteScan = 0; //是否完成扫码后添加进去的，默认值0，1是暂停扫描前添加进去的数据，-1是暂停扫描后添加进去的数据
    private String pinyinName;
    private int categoryIndex;//采样率和码流对应分类序号
    private int randomCoverIndex;//随机默认封面图片index
    private int diskNumber;
    private String composer;
    private String pinyinComposer;//作曲家
    private String MQAReplayGain;//回放增益单曲
    private String replayGainAlbum;//回放增益专辑
    private String path;
    private String displayExtension = null;//用于展示的扩展名
    private boolean isLossless;//是否是列表
    private boolean isLocalCueSong;//是否是本地Cue歌曲
    private String imageUrl;
    private String albumArt;


    public boolean bindFolder(long folderId) {
        this.folderId = folderId;
        if (TextUtils.isEmpty(sources)) {
            sources = String.valueOf(folderId);
            return true;
        } else {
            if (!sources.matches(".*\\b" + folderId + "\\b.*")) {
                sources += " " + folderId;
                return true;
            }
        }
        return false;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.albumArt = imageUrl;
    }

    public void unBindFolder(long folderId) {
        if (!TextUtils.isEmpty(sources)) {
            sources = sources.replaceAll("\\b[\\s]?" + folderId + "[\\s]?\\b", " ").trim();
        }
    }

    public long getGenreId() {
        return genreId;
    }

    public void setGenreId(long genreId) {
        this.genreId = genreId;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getInfoId() {
        return infoId;
    }

    public void setInfoId(long infoId) {
        this.infoId = infoId;
    }

    public long getAddedTime() {
        return this.addedTime;
    }

    public void setAddedTime(long addedTime) {
        this.addedTime = addedTime;
    }

    public long getLastPlayTime() {
        return this.lastPlayTime;
    }

    public void setLastPlayTime(long lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public int getPlayPoint() {
        return this.playPoint;
    }

    public void setPlayPoint(int playPoint) {
        this.playPoint = playPoint;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getSources() {
        return this.sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public long getModifiedTime() {
        return this.modifiedTime;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }


    public boolean isFavor() {
        return favor;
    }

    public void setFavor(boolean favor) {
        this.favor = favor;
    }

    public void setTitle(String title) {
        this.title = title;
        sort = 0;//标题变化，要重新排序
    }

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getExtension() {
        return this.extension;
    }

    public boolean isCue() {
        return (audioType & 0xF00) == 0x100;
    }

    public boolean isDsf() {
        return (audioType & 0xF00) == 0x200;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getAudioType() {
        return audioType;
    }

    public void setAudioType(int audioType) {
        this.audioType = audioType;
    }

    public long getListId() {
        return listId;
    }

    public void setListId(long listId) {
        this.listId = listId;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    //有可能为空
    public String getArtistIds() {
        return artistIds;
    }

    public void setArtistIds(String artistIds) {
        this.artistIds = artistIds;
    }

    public int getPlayTimes() {
        return this.playTimes;
    }

    public void setPlayTimes(int playTimes) {
        this.playTimes = playTimes;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getGenre() {
        return genre;
    }

    public void setGenre(int genre) {
        this.genre = genre;
    }

    public boolean isSong() {
        return (audioType & 0x00F) == TYPE_SONG;
    }

    public boolean isTemp() {
        return (audioType & 0x1000) != 0;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getFavor() {
        return this.favor;
    }

    public String getLrcFileName() {
        return lrcFileName;
    }

    public void setLrcFileName(String lrcFileName) {
        this.lrcFileName = lrcFileName;
    }

    public long getFavorTime() {
        return this.favorTime;
    }

    public void setFavorTime(long favorTime) {
        this.favorTime = favorTime;
    }

    public int getBitrate() {
        return this.bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }


    public int getChannels() {
        return this.channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public int getBits() {
        return this.bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public int getSampleRate() {
        return this.SampleRate;
    }

    public void setSampleRate(int SampleRate) {
        this.SampleRate = SampleRate;
    }

    public String getGenreName() {
        return this.genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public int getTag() {
        return this.tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public void tagMediaLoaded() {
        tag = TAG_MEDIA;
    }

    public boolean isMediaLoaded() {
        return tag == TAG_MEDIA;
    }


    public int getGenreIndex() {
        return this.genreIndex;
    }


    public void setGenreIndex(int genreIndex) {
        this.genreIndex = genreIndex;
    }


    public String getCodec() {
        return this.codec;
    }


    public void setCodec(String codec) {
        this.codec = codec;
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
    public String toString() {
        return "AudioInfo{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", uri='" + uri + '\'' +
                ", title='" + title + '\'' +
                ", albumId=" + albumId +
                ", album='" + album + '\'' +
                ", artistIds='" + artistIds + '\'' +
                ", artist='" + artist + '\'' +
                ", infoId=" + infoId +
                ", addedTime=" + addedTime +
                ", lastPlayTime=" + lastPlayTime +
                ", playPoint=" + playPoint +
                ", duration=" + duration +
                ", audioType=" + audioType +
                ", listId=" + listId +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", number=" + number +
                ", state=" + state +
                ", sources='" + sources + '\'' +
                ", modifiedTime=" + modifiedTime +
                ", playTimes=" + playTimes +
                ", extension='" + extension + '\'' +
                ", genre=" + genre +
                ", date='" + date + '\'' +
                ", lrcFileName='" + lrcFileName + '\'' +
                ", favor=" + favor +
                ", favorTime=" + favorTime +
                ", bitrate=" + bitrate +
                ", SampleRate=" + SampleRate +
                ", channels=" + channels +
                ", bits=" + bits +
                ", genreIndex=" + genreIndex +
                ", genreName='" + genreName + '\'' +
                ", codec='" + codec + '\'' +
                ", tag=" + tag +
                ", sort=" + sort +
                ", exist=" + exist +
                ", path='" + path + '\'' +
                ", displayExtension='" + displayExtension + '\'' +
                '}';
    }

    public String getRecordingMid() {
        return this.recordingMid;
    }


    public void setRecordingMid(String recordingMid) {
        this.recordingMid = recordingMid;
    }


    public String getArtistMid() {
        return this.artistMid;
    }


    public void setArtistMid(String artistMid) {
        this.artistMid = artistMid;
    }


    public String getReleaseMid() {
        return this.releaseMid;
    }


    public void setReleaseMid(String releaseMid) {
        this.releaseMid = releaseMid;
    }


    public boolean isMQA() {//存储数据使用绕过上述是否是MQA机型判断
        return this.isMQA;
    }


    public void setIsMQA(boolean isMQA) {
        this.isMQA = isMQA;
    }


    public void setMqaMode(int mqaMode) {
        this.mqaMode = mqaMode;
    }

    public int getMqaOutSampleRate() {
        return mqaOutSampleRate;
    }

    public void setMqaOutSampleRate(int mqaOutSampleRate) {
        this.mqaOutSampleRate = mqaOutSampleRate;
    }


    public long getFolderId() {
        return this.folderId;
    }


    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }


    public int getIsCompleteScan() {
        return this.isCompleteScan;
    }


    public void setIsCompleteScan(int isCompleteScan) {
        this.isCompleteScan = isCompleteScan;
    }

    public boolean isLossless() {
        return isLossless;
    }

    public void setLossless(boolean lossless) {
        isLossless = lossless;
    }


    public void setPinyinName(String pinyinName) {
        this.pinyinName = pinyinName;
    }


    public String getAlbumArtist() {
        return this.albumArtist;
    }


    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }


    public Long getHashcode() {
        return this.hashcode;
    }


    public void setHashcode(Long hashcode) {
        this.hashcode = hashcode;
    }


    public int getIsVa() {
        return this.isVa;
    }


    public void setIsVa(int isVa) {
        this.isVa = isVa;
    }


    public int getChannels_second() {
        return this.channels_second;
    }

    public void setChannels_second(int channels_second) {
        this.channels_second = channels_second;
    }

    public int getCategoryIndex() {
        return this.categoryIndex;
    }

    public void setCategoryIndex(int categoryIndex) {
        this.categoryIndex = categoryIndex;
    }

    public String getPinyinAlbum() {
        return this.pinyinAlbum;
    }

    public void setPinyinAlbum(String pinyinAlbum) {
        this.pinyinAlbum = pinyinAlbum;
    }

    public String getPinyinArtist() {
        return this.pinyinArtist;
    }

    public void setPinyinArtist(String pinyinArtist) {
        this.pinyinArtist = pinyinArtist;
    }

    public String getPinyinAlbumArtist() {
        return this.pinyinAlbumArtist;
    }

    public void setPinyinAlbumArtist(String pinyinAlbumArtist) {
        this.pinyinAlbumArtist = pinyinAlbumArtist;
    }

    public int getMQAUiState() {
        return this.MQAUiState;
    }

    public void setMQAUiState(int MQAUiState) {
        this.MQAUiState = MQAUiState;
    }

    public int getRandomCoverIndex() {
        return this.randomCoverIndex;
    }

    public void setRandomCoverIndex(int randomCoverIndex) {
        this.randomCoverIndex = randomCoverIndex;
    }

    public boolean isLocalCueSong() {
        return isLocalCueSong;
    }

    public void setLocalCueSong(boolean localCueSong) {
        this.isLocalCueSong = localCueSong;
    }

    public int getDiskNumber() {
        return this.diskNumber;
    }

    public void setDiskNumber(int diskNumber) {
        this.diskNumber = diskNumber;
    }

    public String getComposer() {
        return this.composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getMQAReplayGain() {
        return MQAReplayGain;
    }

    public void setMQAReplayGain(String MQAReplayGain) {
        this.MQAReplayGain = MQAReplayGain;
    }

    public String getReplayGainAlbum() {
        return replayGainAlbum;
    }

    public void setReplayGainAlbum(String replayGainAlbum) {
        this.replayGainAlbum = replayGainAlbum;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComposerIds() {
        return this.composerIds;
    }

    public void setComposerIds(String composerIds) {
        this.composerIds = composerIds;
    }

    public List<String> getComposerIdList() {
        return this.composerIdList;
    }

    public void setComposerIdList(List<String> composerIdList) {
        this.composerIdList = composerIdList;
    }

    public String getPinyinComposer() {
        return this.pinyinComposer;
    }

    public void setPinyinComposer(String pinyinComposer) {
        this.pinyinComposer = pinyinComposer;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
