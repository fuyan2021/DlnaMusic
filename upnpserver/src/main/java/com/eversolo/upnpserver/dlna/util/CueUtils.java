package com.eversolo.upnpserver.dlna.util;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.eversolo.upnpserver.dlna.dms.bean.CharsetDetector;
import com.eversolo.upnpserver.dlna.dms.bean.CueInfo;
import com.eversolo.upnpserver.dlna.dms.bean.CueList;
import com.eversolo.upnpserver.dlna.dms.bean.FileType;
import com.eversolo.upnpserver.dlna.dms.bean.IMusic;
import com.eversolo.upnpserver.dlna.dms.bean.WebMusic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 音乐原盘解析工具
 */
public class CueUtils {

    /**
     * 转化为CUE文件
     *
     * @param dir  文件夹目录
     * @param name 文件名字
     * @return CUE File
     */
    public static File findCueFile(String dir, String name) {
        File cue = new File(dir, name + ".cue");
        if (cue.exists()) {
            return cue;
        }
        cue = new File(dir, name + ".CUE");
        if (cue.exists()) {
            return cue;
        }
        return null;
    }


    /**
     * parse cue file
     * <p>
     * file
     *
     * @return CueFileBean
     */
    public static CueList parseCueFile(File file) {
        LineNumberReader reader = null;
        CueList list = new CueList();
        List<CueInfo> songs = new ArrayList<>();
        list.setSongs(songs);
        CueInfo last = null;
        CueInfo current = null;

        try {
            String charset = getCharset(file);
            if (charset == null) {
                return list;
            }
            reader = new LineNumberReader(new InputStreamReader(new FileInputStream(file), charset));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                try {
                    if (line.startsWith("PERFORMER")) {
                        if (current == null) {
                            list.setPerformer(removeQuotationMarks(line));
                        } else {
                            current.setPerformer(removeQuotationMarks(line));
                        }
                    } else if (line.startsWith("TITLE")) {// 唱片名
                        if (current == null) {
                            list.setAlbumName(removeQuotationMarks(line));
                        } else {
                            current.setTitle(filterNumber(removeQuotationMarks(line)));
                        }
                    } else if (line.startsWith("FILE")) {// 光盘映像文件
                        list.setFileName(removeQuotationMarks(line));
                    } else if (line.startsWith("TRACK")) {// 歌曲编号
                        last = current;
                        current = new CueInfo();
                        songs.add(current);
                    } else if (line.startsWith("INDEX")) {
                        String[] ss = line.split(" ");
                        switch (ss[1]) {
                            case "00":
//								if (last != null) {
//									last.setIndexEnd(ss[2]);
//								}
                                break;
                            case "01":
                                if (last != null) {
                                    last.setIndexEnd(ss[2]);
                                }
                                if (current != null) {
                                    current.setIndexBegin(ss[2]);
                                }
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!songs.isEmpty()) {
                for (CueInfo song : songs) {
                    checkTitleArtist(song);
                }
            }
            if (list.getPerformer() == null && songs.size() > 0) {
                String performer = null;
                for (CueInfo cueInfo : songs) {
                    if (performer == null) {
                        performer = cueInfo.getPerformer();
                    } else {
                        if (!performer.equals(cueInfo.getPerformer())) {
                            performer = null;
                            break;
                        }
                    }
                }
                list.setPerformer(performer);
            }
        } catch (Exception e) {
            Log.e("CueUtils", "parseCueFile", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static CueList parseWebCueFile(InputStream inputStream) {
        LineNumberReader reader = null;
        CueList list = null;
        List<CueInfo> songs = new ArrayList<>();
        CueInfo last = null;
        CueInfo current = null;
        try {
            list = new CueList();
            list.setSongs(songs);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            InputStream stream1 = new ByteArrayInputStream(baos.toByteArray());
            InputStream stream2 = new ByteArrayInputStream(baos.toByteArray());
            String charset = CharsetDetector.getCharset(stream1);
//			Log.i("lgh","charset:" + charset);
            reader = new LineNumberReader(new InputStreamReader(stream2, charset));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("PERFORMER")) {
                    if (current == null) {
                        list.setPerformer(removeQuotationMarks(line));
                    } else {
                        current.setPerformer(removeQuotationMarks(line));
                    }
                } else if (line.startsWith("TITLE")) {// 唱片名
                    if (current == null) {
                        list.setAlbumName(removeQuotationMarks(line));
                    } else {
                        current.setTitle(filterNumber(removeQuotationMarks(line)));
                    }
                } else if (line.startsWith("FILE")) {// 光盘映像文件
                    list.setFileName(removeQuotationMarks(line));
                } else if (line.startsWith("TRACK")) {// 歌曲编号
                    last = current;
                    current = new CueInfo();
                    songs.add(current);
                } else if (line.startsWith("INDEX")) {
                    String[] ss = line.split(" ");
                    switch (ss[1]) {
                        case "00":
//								if (last != null) {
//									last.setIndexEnd(ss[2]);
//								}
                            break;
                        case "01":
                            if (last != null) {
                                last.setIndexEnd(ss[2]);
                            }
                            if (current != null) {
                                current.setIndexBegin(ss[2]);
                            }
                            break;
                    }
                }
            }
            if (!songs.isEmpty()) {
                for (CueInfo song : songs) {
                    checkTitleArtist(song);
                }
            }
            if (list.getPerformer() == null && songs.size() > 0) {
                String performer = null;
                for (CueInfo cueInfo : songs) {
                    if (performer == null) {
                        performer = cueInfo.getPerformer();
                    } else {
                        if (!performer.equals(cueInfo.getPerformer())) {
                            performer = null;
                            break;
                        }
                    }
                }
                list.setPerformer(performer);
            }
        } catch (Exception e) {
            Log.e("CueUtils", "parseCueFile", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    //	//解析云盘CUE封装网络歌曲列表
    public static List<WebMusic> parseWebCueList(String url, String name, CueList cueList) {
        if (cueList == null || cueList.getSongs().isEmpty()) {
            return new ArrayList<>();
        }
        List<CueInfo> cueInfoS = cueList.getSongs();
        String ext = FileType.getFileExt(name);
        List<WebMusic> songs = new ArrayList<>();
        long endTime = 0;

        TrackInfo trackInfo = MediaInfoTool.getTrackInfo(Uri.parse(url).toString());
        if (trackInfo != null) {
            int duration = trackInfo.getDuration();
            for (int i = cueInfoS.size() - 1; i >= 0; i--) {
                CueInfo cue = cueInfoS.get(i);
                long start = parseTime(cue.getIndexBegin());
                long end = parseTime(cue.getIndexEnd());
                if (end == 0 && endTime != 0) {
                    end = endTime;
                }
                if ((start == 0 && i != 0) || (end == 0 && i != cueInfoS.size() - 1)) {
                    //解析有问题，跳过这个文件
                    continue;
                }
                if (end < start) {
                    end = endTime;
                }
                WebMusic song = new WebMusic();
                song.setExtension(ext);
                song.setStartPosition(start);
                song.setEndPosition(end);
                if (end == 0 && i == cueInfoS.size() - 1) {
                    if (duration > 0) {
                        song.setDuration((int) (duration - start));
                    }
                } else {
                    song.setDuration((int) (end - start));
                }
                song.setMQA(trackInfo.getisMQA());
                song.setMqaMode(trackInfo.getAudioType());
                song.setMQAUiState(trackInfo.getMQAUiState());
                song.setMqaOutSampleRate(trackInfo.getMQAOutputSampleRate());
                song.setPreScan(true);
                song.setChannels(trackInfo.getChannels());
                song.setSampleRate(trackInfo.getSamplerate());
                song.setBitrate(trackInfo.getBitrate());
                song.setBits(trackInfo.getBitsPerSample());
                song.setCodec(trackInfo.getCodec());
                song.setMQAReplayGain(trackInfo.getMQAReplayGain());
                song.setReplayGainAlbum(trackInfo.getReplayGainAlbum());
                //如果存在一首歌曲时长小于两分钟直接返回空
                if (song.getDuration() < 2 * 60 * 1000 && song.getDuration() > 0) {
                    return Collections.emptyList();
                }
                song.setNumber(i + 1);
                String title = cue.getTitle();
                String artist = cue.getPerformer();
                int titleIndex = -1, artistIndex = -1;
                if (title == null) {
                    title = (i + 1) + "";
                    song.setName(title);
                } else {
                    song.setName(title);
                    titleIndex = title.indexOf("-");
                    if (artist == null) {
                        song.setArtist(toValidArtist(cueList.getPerformer()));
                    } else {
                        artistIndex = artist.indexOf("-");
                        song.setArtist(toValidArtist(artist));
                    }
                }

                if (titleIndex != -1 && artistIndex != -1) {
                    song.setName(title.substring(titleIndex + 1));
                    song.setArtist(toValidArtist(artist.substring(0, artistIndex)));
                }
                songs.add(song);
                endTime = start;
            }
        }
        Collections.sort(songs, getWebCueListPositionComparator());
        return songs;
    }

    //	//解析云盘SACD封装网络歌曲列表
    public static List<WebMusic> parseWebCueList(String name, CueList cueList) {
        if (cueList == null || cueList.getSongs().isEmpty()) {
            return new ArrayList<>();
        }
        List<CueInfo> cueInfoS = cueList.getSongs();
        String ext = FileType.getFileExt(name);
        List<WebMusic> songs = new ArrayList<>();
        long endTime = 0;

        for (int i = cueInfoS.size() - 1; i >= 0; i--) {
            CueInfo cue = cueInfoS.get(i);
            long start = parseTime(cue.getIndexBegin());
            long end = parseTime(cue.getIndexEnd());
            if (end == 0 && endTime != 0) {
                end = endTime;
            }
            if ((start == 0 && i != 0) || (end == 0 && i != cueInfoS.size() - 1)) {
                //解析有问题，跳过这个文件
                continue;
            }
            if (end < start) {
                end = endTime;
            }
            WebMusic song = new WebMusic();
            song.setExtension(ext);
            if (end == 0 && i == cueInfoS.size() - 1) {
                song.setDuration(-2);
            } else {
                song.setDuration((int) (end - start));
            }
            song.setNumber(i + 1);
            String title = cue.getTitle();
            String artist = cue.getPerformer();
            int titleIndex = -1, artistIndex = -1;
            if (title == null) {
                title = (i + 1) + "";
                song.setName(title);
            } else {
                song.setName(title);
                titleIndex = title.indexOf("-");
                if (artist == null) {
                    song.setArtist(toValidArtist(cueList.getPerformer()));
                } else {
                    artistIndex = artist.indexOf("-");
                    song.setArtist(toValidArtist(artist));
                }
            }

            if (titleIndex != -1 && artistIndex != -1) {
                song.setName(title.substring(titleIndex + 1));
                song.setArtist(toValidArtist(artist.substring(0, artistIndex)));
            }
            songs.add(song);
            endTime = start;
        }
        Collections.sort(songs, getWebCueListPositionComparator());
        return songs;
    }

    public static Comparator<IMusic> getWebCueListPositionComparator() {
        return new Comparator<IMusic>() {
            @Override
            public int compare(IMusic o1, IMusic o2) {
                return ((WebMusic) o1).getNumber() - ((WebMusic) o2).getNumber();
            }

        };
    }

    public static String toValidArtist(String artist) {
        if (artist != null && (
                artist.toLowerCase().contains("群星") ||
                        artist.toLowerCase().contains("track") ||
                        artist.toLowerCase().contains("various") ||
                        "va".equalsIgnoreCase(artist) ||
                        artist.toLowerCase().contains("v.a.")
        )) {
//            if (ZidooMusic.isChinese()) {
//                artist = "群星";
//            } else {
//                artist = "Various Artists";
//            }
            artist = "Various Artists";
        }
        return artist;
    }

    private static void checkTitleArtist(CueInfo cue) {
        try {
            String title = cue.getTitle();
            if (title == null) {
                return;
            }
            String artist = cue.getPerformer();
            boolean sameTitleArtist = Objects.equals(title, artist);
            //去掉标题的序号
            if (title.matches("\\d+\\s?[_.、\\-\\s]\\s?.+")) {
                String[] ss = toNumberTitle(title);
                //int number = Integer.parseInt(ss[0]);
                title = ss[1];
                cue.setTitle(title);
                if (sameTitleArtist) {
                    cue.setPerformer(title);
                }
            }
            //分解标题和艺术家
            if (sameTitleArtist) {
                String[] ss = title.split("-");
                if (ss.length == 2) {
                    cue.setPerformer(ss[0].trim());
                    cue.setTitle(ss[1].trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] toNumberTitle(String s) {
        StringBuilder number = new StringBuilder();
        String title = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                number.append(c);
            } else {
                if (c != '.' && c != '、' && c != ' ' && c != '_' && c != '-') {
                    title = s.substring(i);
                    break;
                }
            }
        }
        return new String[]{number.toString(), title};
    }


    private static String removeQuotationMarks(String res) {
        try {
            return res.substring(res.indexOf('\"') + 1, res.lastIndexOf('\"'));
        } catch (Exception e) {
            return res;
        }
    }

    private static String filterNumber(String res) {
        if (res.matches("\\d+\\s?[_.、\\-\\s]\\s?.+")) {
            String[] ss = toNumberTitle(res);
            return ss[1];
        }
        return res;
    }


    /**
     * 获取字符编码
     *
     * @param file 文件
     * @return 字符编码
     */
    public static String getCharset(File file) {
        CharsetDetector detector = new CharsetDetector();
        if (file.exists()) {
            int code = detector.detectEncoding(file, 8192);
            return detector.getDecodeString(code);
        }
        return null;
    }


    /**
     * 用Cue,要访问转一下格式访问文档
     */
    public static File getCueFile(String path) {
        if (path == null) {
            return null;
        }
        int e = path.lastIndexOf('.');
        String pre = path.substring(0, e);
        File cue = new File(pre + ".cue");
        if (cue.exists()) {
            return cue;
        } else {
            cue = new File(pre + ".CUE");
            return cue.exists() ? cue : null;
        }
    }


    public static boolean isCueExt(String str) {
        try {
            if (str == null) {
                return false;
            }
            String ext = str.toLowerCase().trim();
            if (ext.equals("ape") || ext.equals("flac") || ext.equals("cue")) {
                return true;
            }
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return false;
    }

    //	/**
//	 * 7.1版本刚播要间隔一点时间seek
//	 */
//	public static void sevenOneVersion(IPlayer player) {
//		if (player.getCurrentPosition() == 0 && VERSION.RELEASE.startsWith("7")) {
//			try {
//				int sleepTime = 0;
//				do {
//					Thread.sleep(10);
//					if (player.getCurrentPosition() > 0) {
//						break;
//					}
//					sleepTime += 10;
//				} while (sleepTime < 1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}
    private static int[] XS = {0, 1000, 60 * 1000, 60 * 60 * 1000};

    public static long parseTime(String time) {
        if (TextUtils.isEmpty(time)) {
            return 0;
        }
        try {
            long millisecond = 0;
            String[] ss = time.split(":");
            for (int i = ss.length - 1; i >= 0; i--) {
                millisecond += Integer.parseInt(ss[i]) * XS[ss.length - 1 - i];
            }
            return millisecond;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 解析临时文件的CUE
     */
    public static CueList getCueList(File file) {
        String fileName = file.getName();
        int p = fileName.lastIndexOf(".");
        String name = fileName.substring(0, p);
        File cueFile = CueUtils.findCueFile(file.getParent(), name);
        if (cueFile != null) {
            return CueUtils.parseCueFile(cueFile);
        }
        return null;
    }

//	public static List<AudioInfo> parseCueList(File file, CueList cueList, boolean isSACD, boolean isTemp) {
//		if (cueList == null || cueList.getSongs().isEmpty()) {
//			return new ArrayList<>();
//		}
//		List<CueInfo> cueInfoS = cueList.getSongs();
//		String ext = FileType.getFileExt(file.getName());
//		List<AudioInfo> songs = new ArrayList<>();
//		long endTime = 0;
//
//		for (int i = cueInfoS.size() - 1; i >= 0; i--) {
//			CueInfo cue = cueInfoS.get(i);
//			long start = Utils.parseTime(cue.getIndexBegin());
//			long end = Utils.parseTime(cue.getIndexEnd());
//			if (end == 0 && endTime != 0) {
//				end = endTime;
//			}
//			if ((start == 0 && i != 0) || (end == 0 && i != cueInfoS.size() - 1)) {
//				//解析有问题，跳过这个文件
//				continue;
//			}
//			if (end < start) {
//				end = endTime;
//			}
//			AudioInfo song = new AudioInfo();
//			song.setAddedTime(System.currentTimeMillis());
//			song.setRandomCoverIndex(new Random().nextInt(9));
//			song.setExtension(ext);
//			if (isSACD) {
//				if (isTemp) {
//					song.setAudioType(AudioInfo.TYPE_DSF_TEMP_SONG);
//				} else {
//					song.setAudioType(AudioInfo.TYPE_DSF_SONG);
//				}
//			} else {
//				song.setAudioType(AudioInfo.TYPE_CUE_TEMP_SONG);
//				song.setStartPosition(start);
//				song.setEndPosition(end);
//			}
//			song.setPath(file.getPath());
//
//			if (end == 0 && i == cueInfoS.size() - 1) {
//				song.setDuration(-2);
//			} else {
//				song.setDuration((int) (end - start));
//			}
//			song.setNumber(i + 1);
//			int state;
//			String title = cue.getTitle();
//			String artist = cue.getPerformer();
//			int titleIndex = -1, artistIndex = -1;
//			if (title == null) {
//				state = AudioInfo.STATE_PART_NO_TITLE;
//				title = (i + 1) + "";
//				song.setTitle(title);
//				song.setPinyinName(PinyinTool.getNamePingyin(song.getTitle()));
//			} else {
//				state = AudioInfo.STATE_UN_MATCH;
//				song.setTitle(title);
//				song.setPinyinName(PinyinTool.getNamePingyin(song.getTitle()));
//				titleIndex = title.indexOf("-");
//				if (artist == null) {
//					song.setArtist(toValidArtist(cueList.getPerformer()));
//					song.setPinyinArtist(PinyinTool.getNamePingyin(song.getArtist()));
//				} else {
//					artistIndex = artist.indexOf("-");
//					song.setArtist(toValidArtist(artist));
//					song.setPinyinArtist(PinyinTool.getNamePingyin(song.getArtist()));
//				}
//			}
//
//			if (titleIndex != -1 && artistIndex != -1) {
//				song.setTitle(title.substring(titleIndex + 1));
//				song.setPinyinName(PinyinTool.getNamePingyin(song.getTitle()));
//				song.setArtist(toValidArtist(artist.substring(0, artistIndex)));
//				song.setPinyinArtist(PinyinTool.getNamePingyin(song.getArtist()));
//			}
//			String album = cueList.getAlbumName();
//			if(!TextUtils.isEmpty(album)){
//				song.setAlbum(album);
//				song.setPinyinAlbum(PinyinTool.getNamePingyin(album));
//			}
//			song.setState(state);
//			songs.add(song);
//			endTime = start;
//		}
//		Collections.sort(songs, getCueListPositionComparator());
//		return songs;
//	}
//
//	/**
//	 * 解析临时文件的SACD,如果不是原盘专辑音乐或者是单曲目，会返回空列表；如果是多曲目原盘专辑音乐，则返回全列表（数目大于1）
//	 */
//	public static List<AudioInfo> getSACDList(File file) {
//		List<SongInfo> dsfList = getDsfSongInfos(file.getPath());
//		if (Toolc.isEmpty(dsfList)) {
//			return Collections.emptyList();
//		} else if (dsfList.size() == 1) {
//			return Collections.emptyList();
//		}
//
//		String ext = FileType.getFileExt(file.getName());
//		List<AudioInfo> songs = new ArrayList<>();
//
//		List<AudioInfo> isoList = ISOXmlParser.getISOList(file);
//		if (isoList != null && isoList.size() > 0 && isoList.size() == dsfList.size()){
//			for (int i = 0; i < isoList.size(); i++) {
//				AudioInfo song = isoList.get(i);
//				AudioInfo single = new AudioInfo();
//				single.setAddedTime(System.currentTimeMillis());
//				single.setRandomCoverIndex(new Random().nextInt(9));
//				single.setAudioType(AudioInfo.TYPE_DSF_TEMP_SONG);
//				single.setExtension(ext);
//				String title = song.getTitle();
//				if (title == null) {
//					title = (i + 1) + "";
//					single.setState(AudioInfo.STATE_PART_NO_TITLE);
//				} else {
//					single.setState(AudioInfo.STATE_UN_MATCH);
//				}
//				single.setTitle(title);
//				String album = song.getAlbum();
//				if(!TextUtils.isEmpty(album)){
//					single.setAlbum(album);
//					single.setPinyinAlbum(PinyinTool.getNamePingyin(album));
//				}
//				single.setPinyinName(PinyinTool.getNamePingyin(single.getTitle()));
//				single.setArtist(toValidArtist(song.getArtist()));
//				single.setPinyinArtist(PinyinTool.getNamePingyin(single.getArtist()));
//				single.setDuration(song.getDuration());
//				single.setNumber(i + 1);
//				single.setPath(file.getPath());
//				songs.add(single);
//			}
//		}else {
//			for (int i = 0; i < dsfList.size(); i++) {
//				SongInfo song = dsfList.get(i);
//				AudioInfo single = new AudioInfo();
//				single.setAddedTime(System.currentTimeMillis());
//				single.setRandomCoverIndex(new Random().nextInt(9));
//				single.setAudioType(AudioInfo.TYPE_DSF_TEMP_SONG);
//				single.setExtension(ext);
//				String title = song.getTitle();
//				if (title == null) {
//					title = (i + 1) + "";
//					single.setState(AudioInfo.STATE_PART_NO_TITLE);
//				} else {
//					single.setState(AudioInfo.STATE_UN_MATCH);
//				}
//				single.setTitle(title);
//				String album = song.getAlbum();
//				if(!TextUtils.isEmpty(album)){
//					single.setAlbum(album);
//					single.setPinyinAlbum(PinyinTool.getNamePingyin(album));
//				}
//				single.setPinyinName(PinyinTool.getNamePingyin(single.getTitle()));
//				single.setArtist(toValidArtist(song.getArtist()));
//				single.setPinyinArtist(PinyinTool.getNamePingyin(single.getArtist()));
//				single.setDuration(song.getDuration());
//				single.setNumber(i + 1);
//				single.setPath(file.getPath());
//				songs.add(single);
//			}
//		}
//
//		return songs;
//	}
//

}
