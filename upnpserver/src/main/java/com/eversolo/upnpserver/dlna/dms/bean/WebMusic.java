package com.eversolo.upnpserver.dlna.dms.bean;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;

/**
 * 网络音乐
 * Created by LIC on 2018-07-27.
 */
public class WebMusic extends IMusic {

    public static final long serialVersionUID = 1L;

    private static int sId = 1;

    private int id = getSid();
    private String uri;
    private String name = null;
    private String extension = null;
    private String artist;
    private String album;
    private String albumArt;
    private int duration;
    private String musicType = null;
    private long fileSize;
    private int sampleRate;
    private int bitrate;
    private int channels = 0;
    private int sourceType;//网络音乐类型
    private String trackId;//歌曲id
    private String audioQuality;//音频质量
    private boolean isMQA = false;
    private int mqaMode = 0;
    private int MQAUiState = -1;
    private int mqaOutSampleRate = 0;
    private boolean isPreScan = false;
    private boolean isFoucedReload = false;
    private String codec;
    private int bits;
    private long fs_id;//百度网盘文件id
    private String file_id;//阿里云盘文件id
    private String drive_id;//阿里云盘id
    private String streamUrl;
    private String trackUrl;//歌曲地址
    private String mixPrevUrl;//mix上一首地址
    private String mixNextUrl;//mix下一首地址
    private boolean isMix;//是否是Mix音乐
    private String json;
    private String behaviour;//mix歌曲播放操作权限
    private String attributes;//airable歌曲gain、peak值属性

    private long playUrlTime;//播放地址获取时间
    private String events;//亚马逊歌曲事件(start,stop,pause,resume)
    private String radioMusicJson;//RadioParadise当前歌曲json信息；
    private String stationId;
    private int neteasePlayType;//网易云音乐播放歌曲类型NeteasePlayType(指定列表 0、歌单 1、专辑 2、艺术家 3、私人FM 4、场景电台 5、播客声音 6、随心听FM 7)
    private String neteaseListId;//网易云音乐列表歌曲标识id(歌单id,艺术家id,专辑id,场景电台tag)
    private String ratingKey;//Plex、EMBY歌曲id
    private long startPosition;//如果类型为TYPE_CUE_SONG时，表示这首歌的开始对应CUE文件的位置
    private long endPosition;//如果类型为TYPE_CUE_SONG时，表示这首歌的结束对应CUE文件的位置
    private boolean isCueSong;//是否是Cue歌曲
    private int number;//cue歌曲序号
    private boolean isISOMusic;//是否是ISO音乐
    private boolean isFavorite; //是否为收藏歌曲 暂为SoundCloud使用
    private String MQAReplayGain;//回放增益单曲
    private String replayGainAlbum;//回放增益专辑
    private String channelName; //频道的Title,暂为CalmRadio使用
    private String streamId;  //播放流Id,暂为CalmRadio使用
    private int calmVip;  //是否为会员,暂为CalmRadio使用
    private boolean hasNextPlay; //是否能下一曲
    private boolean hasLastPlay; //是否能上一曲
    private int parentId; //索尼精选获取网络音乐列表的父类Id，如专辑ID，歌单ID，榜单ID
    private String auditionUrl; //索尼试听地址
    private int sonyTrackGrade; //索尼歌曲权限，puls会员200, premium会员300
    private boolean isAudition = false; //索尼歌曲試聽
    private int sonyDuration = 0; //索尼歌曲时长
    private boolean isSonyFree = false; //索尼歌曲是否免费
    private List<Integer> boxSteams; //KKBOX歌曲流质量下标
    private boolean isKKBoxRadio = false; //KKBOX是否为电台
    private boolean isKKBoxCanPlay = true; //KKBOX是否能播放
    private String kkBoxQuality = ""; //KKBOX的音频质量
    private int lastPosition = 0;//TuneinRadio播客上一次播放位置
    private int sonyAlbumId = 0;//索尼专辑ID
    private boolean isTuneinCustom;//是否是Tunein自定义网址
    private int workId; //prestoMusic 作品集Id
    private int mode; //数据来源---模式（专辑，歌单，收藏，搜索）
    private boolean isPlayed; //播客列表是否设置已播放过
    private String podcastFeedUrl; // 播客地址

    public boolean isTuneinCustom() {
        return isTuneinCustom;
    }

    public void setTuneinCustom(boolean tuneinCustom) {
        isTuneinCustom = tuneinCustom;
    }

    public int getSonyAlbumId() {
        return sonyAlbumId;
    }

    public void setSonyAlbumId(int sonyAlbumId) {
        this.sonyAlbumId = sonyAlbumId;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }




    public boolean isSonyFree() {
        return isSonyFree;
    }

    public void setSonyFree(boolean sonyFree) {
        isSonyFree = sonyFree;
    }

    public String getKkBoxQuality() {
        return kkBoxQuality;
    }

    public void setKkBoxQuality(String kkBoxQuality) {
        this.kkBoxQuality = kkBoxQuality;
    }

    public boolean isKKBoxCanPlay() {
        return isKKBoxCanPlay;
    }

    public void setKKBoxCanPlay(boolean KKBoxCanPlay) {
        isKKBoxCanPlay = KKBoxCanPlay;
    }

    public int getWorkId() {
        return workId;
    }

    public void setWorkId(int workId) {
        this.workId = workId;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isHasNextPlay() {
        return hasNextPlay;
    }

    public void setHasNextPlay(boolean hasNextPlay) {
        this.hasNextPlay = hasNextPlay;
    }

    public boolean isHasLastPlay() {
        return hasLastPlay;
    }

    public void setHasLastPlay(boolean hasLastPlay) {
        this.hasLastPlay = hasLastPlay;
    }

    public int isCalmVip() {
        return calmVip;
    }

    public void setCalmVip(int calmVip) {
        this.calmVip = calmVip;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isISOMusic() {
        return isISOMusic;
    }

    public void setISOMusic(boolean ISOMusic) {
        isISOMusic = ISOMusic;
    }

    private synchronized static int getSid() {
        return sId++;
    }

    public WebMusic() {
    }

    public WebMusic(String uri) {
        this.uri = uri;
        initMusicType();
    }

    //webdav音乐
    public WebMusic(int id, String uri, String name, long fileSize, String extension, int sourceType) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.fileSize = fileSize;
        this.extension = extension;
        this.sourceType = sourceType;
        initMusicType();
    }

    //百度网盘音乐
    public WebMusic(int id, String uri, long fs_id, String name, long fileSize, String extension, int sourceType) {
        this.id = id;
        this.uri = uri;
        this.fs_id = fs_id;
        this.name = name;
        this.fileSize = fileSize;
        this.extension = extension;
        this.sourceType = sourceType;
        initMusicType();
    }

    //阿里云盘音乐
    public WebMusic(int id, String uri, String driveId, String file_id, String name, long fileSize, String extension, int sourceType) {
        this.id = id;
        this.uri = uri;
        this.drive_id = driveId;
        this.file_id = file_id;
        this.name = name;
        this.fileSize = fileSize;
        this.extension = extension;
        this.sourceType = sourceType;
        initMusicType();
    }

    //OneDrive、DropBox
    public WebMusic(int id, String uri, String file_id, String name, long fileSize, String extension, int sourceType) {
        this.id = id;
        this.uri = uri;
        this.file_id = file_id;
        this.name = name;
        this.fileSize = fileSize;
        this.extension = extension;
        this.sourceType = sourceType;
        initMusicType();
    }

    public WebMusic(int id, String uri, String name, int duration, String artist, String albumArt) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.artist = artist;
        this.albumArt = albumArt;
        initMusicType();
    }

    public WebMusic(int id, String uri, String name, int duration, String artist, String albumArt, String musicType, long fileSize,
                    int sourceType) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.artist = artist;
        this.albumArt = albumArt;
        this.musicType = musicType;
        this.fileSize = fileSize;
        this.sourceType = sourceType;
    }

    //Tidal在线音乐
    public WebMusic(int id, String uri, String trackId, String name, int duration, String artist, String albumArt, String audioQuality,
                    String extension, int sourceType) {
        this.id = id;
        this.uri = uri;
        this.trackId = trackId;
        this.name = name;
        this.duration = duration;
        this.artist = artist;
        this.albumArt = albumArt;
        this.audioQuality = audioQuality;
        this.extension = extension;
        this.sourceType = sourceType;
        initMusicType();
    }

    ////Plex音乐和EMBY音乐
    public WebMusic(int id, String uri, String ratingKey, String name, long fileSize, String extension, String artist, String albumName, String albumArt, int sourceType) {
        this.id = id;
        this.uri = uri;
        this.ratingKey = ratingKey;
        this.name = name;
        this.fileSize = fileSize;
        this.extension = extension;
        this.artist = artist;
        this.album = albumName;
        this.albumArt = albumArt;
        this.sourceType = sourceType;
        initMusicType();
    }

    //UPnP音乐
    public WebMusic(int id, String uri, String name, int duration, String artist, String albumArt, String musicType, long fileSize,
                    int sampleRate, int channels, int sourceType) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.artist = artist;
        this.albumArt = albumArt;
        this.musicType = musicType;
        this.fileSize = fileSize;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.sourceType = sourceType;
    }

    //播客
    public WebMusic(int id, String uri, String name, int duration, String albumArt,boolean isPlayed,String podcastFeedUrl, int sourceType) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.albumArt = albumArt;
        this.sourceType = sourceType;
        this.extension = "web";
        this.isPlayed = isPlayed;
        this.podcastFeedUrl = podcastFeedUrl;
        initMusicType();
    }

    //RadioParadise歌曲
    public WebMusic(int id, String uri, String name, String artist, String album, String albumArt, String extension, String radioMusicJson, int sourceType){
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.albumArt = albumArt;
        this.extension = extension;
        this.radioMusicJson = radioMusicJson;
        this.sourceType = sourceType;
        initMusicType();
    }

    //SoundCloud声音
    public WebMusic(int id, String uri, String name, String artist, String albumArt, String extension, int sourceType, boolean like) {
        this.id = id;
        this.ratingKey = id + "";
        this.uri = uri;
        this.artist = artist;
        this.albumArt = albumArt;
        this.name = name;
        this.extension = extension;
        this.sourceType = sourceType;
        this.isFavorite = like;
        initMusicType();
    }

    //KKBOX声音
    public WebMusic(String id, String uri, String name, String artist, String albumArt, String extension, int sourceType, boolean like) {
        this.ratingKey = id;
        this.id = id.hashCode();
        this.uri = uri;
        this.artist = artist;
        this.albumArt = albumArt;
        this.name = name;
        this.extension = extension;
        this.sourceType = sourceType;
        this.isFavorite = like;
        initMusicType();
    }


    //CalmRadio在线服务音乐
    public WebMusic(String id, String streamId, String streamUrl, String name, String albumArt, int sourceType) {
        this.ratingKey = id;
        this.id = id.hashCode();
        this.streamId = streamId;
        this.uri = streamUrl;
        this.name = name;
        this.channelName = name;
        this.albumArt = albumArt;
        this.sourceType = sourceType;
        initMusicType();
    }

    //PrestoMusic在线服务音乐
    public WebMusic(int id, String ratingKey, String name, String uri, int sourceType) {
        this.ratingKey = ratingKey;
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.sourceType = sourceType;
        initMusicType();
    }

    private void initMusicType() {
        try {
            if (musicType == null && uri != null && uri.contains(".")) {
                musicType = uri.substring(uri.lastIndexOf(".") + 1).toLowerCase();
                if (musicType.equals("iso")) {
                    musicType = "SACD";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public WebMusic(int id, String uri, String name, int duration, String albumArt, int sourceType) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.albumArt = albumArt;
        this.sourceType = sourceType;
        this.extension = "web";
        initMusicType();
    }




    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public boolean isAmazonMusic() {
        return sourceType == WebMusicType.ONLINE_AMAZON_MUSIC;
    }

    public boolean isTidalMusic() {
        return sourceType == WebMusicType.ONLINE_TIDAL_MUSIC;
    }

    public boolean isRadioParadiseMusic() {
        return sourceType == WebMusicType.RADIO_PARADISE_MUSIC;
    }

    public boolean isRadioBrowserMusic() {
        return sourceType == WebMusicType.RADIO_BROWSER;
    }

    public boolean isNeteaseCloudMusic() {
        return sourceType == WebMusicType.NETEASE_CLOUD_MUSIC;
    }

    public boolean isPrestoMusic() {
        return sourceType == WebMusicType.PRESTO_MUSIC;
    }
    public boolean isPodcast() {
        return sourceType == WebMusicType.PODCAST;
    }

    public boolean isQobuzMusic() {
        return sourceType == WebMusicType.ONLINE_QOBUZ_MUSIC;
    }
    public boolean isIdagioMusic(){
        return sourceType == WebMusicType.ONLINE_IDAGIO_MUSIC;
    }
    public boolean isEmbyMusic(){
        return sourceType == WebMusicType.EMBY_MUSIC;
    }
    public boolean isJellyFinMusic() {
        return sourceType == WebMusicType.JELLY_FIN_MUSIC;
    }

    public boolean isKKBoxMusic() {
        return sourceType == WebMusicType.KKBOX_MUSIC;
    }
    public boolean isSonyMusic() {
        return sourceType == WebMusicType.SONY_MUSIC;
    }

    public boolean isSoundCloudMusic() {
        return sourceType == WebMusicType.SOUND_CLOUD_MUSIC;
    }

    public boolean isCalmRadio() {
        return sourceType == WebMusicType.CALM_RADIO;
    }

    public boolean isPlexMusic(){
        return sourceType == WebMusicType.PLEX_MUSIC;
    }

    public boolean isTuneinRadio() {
        return sourceType == WebMusicType.TUNEIN_RADIO;
    }

    public boolean isAppleMusic() {
        return sourceType == WebMusicType.APPLE_MUSIC;
    }

    public boolean isMQA() {
        if(extension != null && extension.equals("MQA")){
            return true;
        }
        return isMQA;
    }

    public void setMQA(boolean MQA) {
        isMQA = MQA;
    }

    @Override
    public int getMqaMode() {
        return mqaMode;
    }

    public void setMqaMode(int mqaMode) {
        this.mqaMode = mqaMode;
    }

    @Override
    public int getMQAUiState() {
        return MQAUiState;
    }

    public void setMQAUiState(int MQAUiState) {
        this.MQAUiState = MQAUiState;
    }

    public String getAudioQuality() {
        return audioQuality;
    }

    public void setAudioQuality(String audioQuality) {
        this.audioQuality = audioQuality;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        if(bitrate < 0){
            return;
        }
        this.bitrate = bitrate;
    }

    @Override
    public int getChannels() {
        return channels;
    }

    public int getMqaOutSampleRate() {
        return mqaOutSampleRate;
    }

    public void setMqaOutSampleRate(int mqaOutSampleRate) {
        this.mqaOutSampleRate = mqaOutSampleRate;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public String getMusicType() {
        return musicType;
    }

    public void setMusicType(String musicType) {
        this.musicType = musicType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isDsf() {
        if (musicType != null) {
            return musicType.equals("SACD") || musicType.equals("dsf") || musicType.equals("dff");
        }
        return false;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public long getMusicId() {
        return id;
    }

    public void setMusicId(int id) {
        this.id = id;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    public boolean isPreScan() {
        return isPreScan;
    }

    public void setPreScan(boolean preScan) {
        isPreScan = preScan;
    }

    @Override
    public MusicType getType() {
        return MusicType.WEB;
    }

    @Override
    public String getPath() {
        return uri;
    }

    @Override
    public String getTitle() {
        if (name == null) {
            initNameAndExt();
        }
        return name;
    }

    @Override
    public String getArtist() {
        return artist;
    }

    @Override
    public String getAlbum() {
        return album;
    }

    @NonNull
    @Override
    public String getDisplayExtension() {
        if (TextUtils.isEmpty(extension) && !isAmazonMusic() && !isSonyMusic()) {
            initNameAndExt();
        }
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    private void initNameAndExt() {
        String temp = uri.toString();
        if (temp.endsWith("/")) {
            temp = temp.substring(0, temp.length() - 1);
        }
        int p = temp.lastIndexOf('/');
        if (p != -1) {
            temp = temp.substring(p + 1);
        }
        p = temp.lastIndexOf('.');
        if (p == -1) {
            if (name == null) {
                name = temp;
            }
            if (uri.startsWith("http")) {
                extension = "web";
            } else {
                extension = "";
            }
        } else {
            if (name == null) {
                name = temp.substring(0, p);
            }
            String newTemp = temp.substring(p + 1);
            int index = newTemp.indexOf("?");
            if(index != -1){
                extension = newTemp.substring(0,index);
            }else {
                extension = newTemp;
            }
        }
    }

    @Override
    public boolean isSameAs(IMusic other) {
        if (this == other) {
            return true;
        }
        if (other == null || other.getType() != MusicType.WEB) {
            return false;
        }
        if(other instanceof WebMusic){
            //如果是网络cue歌曲判断id是否一样
            if(((WebMusic) other).isCueSong() || ((WebMusic) other).isISOMusic){
                return getMusicId() == other.getMusicId();
            }
        }
        return getPath().equals(other.getPath());
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public long getFs_id() {
        return fs_id;
    }

    public void setFs_id(long fs_id) {
        this.fs_id = fs_id;
    }

    public String getFile_id() {
        return file_id;
    }

    public void setFile_id(String file_id) {
        this.file_id = file_id;
    }

    public String getDrive_id() {
        return drive_id;
    }

    public void setDrive_id(String drive_id) {
        this.drive_id = drive_id;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
    }

    public String getMixPrevUrl() {
        return mixPrevUrl;
    }

    public void setMixPrevUrl(String mixPrevUrl) {
        this.mixPrevUrl = mixPrevUrl;
    }

    public String getMixNextUrl() {
        return mixNextUrl;
    }

    public void setMixNextUrl(String mixNextUrl) {
        this.mixNextUrl = mixNextUrl;
    }

    public boolean isMix() {
        return isMix;
    }

    public void setMix(boolean mix) {
        isMix = mix;
    }

    //是否是Airable服务音乐
    public boolean isAirableMusic() {
        switch (sourceType) {
            case WebMusicType.ONLINE_TIDAL_MUSIC:
            case WebMusicType.ONLINE_QOBUZ_MUSIC:
            case WebMusicType.ONLINE_HIGHRES_AUDIO:
            case WebMusicType.ONLINE_AMAZON_MUSIC:
            case WebMusicType.ONLINE_DEEZER_AUDIO:
            case WebMusicType.ONLINE_IDAGIO_MUSIC:
                return true;
        }
        return false;
    }

    //是否是LMS服务音乐
    public boolean isLmsMusic() {
        return sourceType == WebMusicType.LMS;
    }

    public boolean isQplayMusic() {
        return sourceType == WebMusicType.DLNA_QPLAY;
    }

    //是否是需要获取播放地址网络歌曲
    public boolean isGetWebUrlMusic(){
        if (isAirableMusic() || sourceType == WebMusicType.BAIDU_NETDISK_MUSIC || sourceType == WebMusicType.ALIYUNDRIVE_MUSIC
                || sourceType == WebMusicType.ONEDRIVE_MUSIC || sourceType == WebMusicType.DROPBOX_MUSIC
                || sourceType == WebMusicType.RADIO_PARADISE_MUSIC || sourceType == WebMusicType.NETEASE_CLOUD_MUSIC
                || sourceType == WebMusicType.SOUND_CLOUD_MUSIC || (sourceType == WebMusicType.TUNEIN_RADIO && !isTuneinCustom) || sourceType == WebMusicType.SONY_MUSIC
                || sourceType == WebMusicType.KKBOX_MUSIC || sourceType == WebMusicType.PRESTO_MUSIC) {
            return true;
        }
        return false;
    }

    public boolean isFoucedReload() {
        return isFoucedReload;
    }

    public void setFoucedReload(boolean foucedReload) {
        isFoucedReload = foucedReload;
    }

    public long getPlayUrlTime() {
        return playUrlTime;
    }

    public void setPlayUrlTime(long playUrlTime) {
        this.playUrlTime = playUrlTime;
    }

    public String getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(String behaviour) {
        this.behaviour = behaviour;
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        this.events = events;
    }

    public String getRadioMusicJson() {
        return radioMusicJson;
    }

    public void setRadioMusicJson(String radioMusicJson) {
        this.radioMusicJson = radioMusicJson;
    }


    public String getNeteaseListId() {
        return neteaseListId;
    }

    public void setNeteaseListId(String neteaseListId) {
        this.neteaseListId = neteaseListId;
    }

    public int getNeteasePlayType() {
        return neteasePlayType;
    }

    public void setNeteasePlayType(int neteasePlayType) {
        this.neteasePlayType = neteasePlayType;
    }

    public String getRatingKey() {
        return ratingKey;
    }

    public void setRatingKey(String ratingKey) {
        this.ratingKey = ratingKey;
    }

    @Override
    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public boolean isCueSong() {
        return isCueSong;
    }

    public void setCueSong(boolean cueSong) {
        isCueSong = cueSong;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getExtension() {
        return extension;
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

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getAuditionUrl() {
        return auditionUrl;
    }

    public void setAuditionUrl(String auditionUrl) {
        this.auditionUrl = auditionUrl;
    }

    public int getSonyTrackGrade() {
        return sonyTrackGrade;
    }

    public void setSonyTrackGrade(int sonyTrackGrade) {
        this.sonyTrackGrade = sonyTrackGrade;
    }

    public boolean isAudition() {
        return isAudition;
    }

    public void setAudition(boolean audition) {
        isAudition = audition;
    }

    public int getSonyDuration() {
        return sonyDuration;
    }

    public void setSonyDuration(int sonyDuration) {
        this.sonyDuration = sonyDuration;
    }

    public List<Integer> getBoxSteams() {
        return boxSteams;
    }

    public String getBoxSteamsString() {
        if (boxSteams == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < boxSteams.size(); i++) {
            sb.append(boxSteams.get(i));
            if (i != boxSteams.size() -1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
    public void setBoxSteams(List<Integer> boxSteams) {
        this.boxSteams = boxSteams;
    }

    public boolean isKKBoxRadio() {
        return isKKBoxRadio;
    }

    public void setKKBoxRadio(boolean KKBoxRadio) {
        isKKBoxRadio = KKBoxRadio;
    }

    public boolean isPlayed() {
        return isPlayed;
    }

    public void setPlayed(boolean played) {
        isPlayed = played;
    }

    public String getPodcastFeedUrl() {
        return podcastFeedUrl;
    }

    public void setPodcastFeedUrl(String podcastFeedUrl) {
        this.podcastFeedUrl = podcastFeedUrl;
    }
}
