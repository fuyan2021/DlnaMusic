package org.fourthline.cling.support.qplay;

import android.util.Log;

import com.google.gson.Gson;
import com.zxt.dlna.dmr.ZxtMediaPlayer;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.lastchange.LastChange;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Description: QPlay服务具体实现 <br>
 *
 * @author: fy <br>
 * Date: 2024/2/20 <br>
 */
public class DmrQPlayService extends AbstractQPlayService {
    private final String TAG = "DmrQPlayService";
    private final String MID = "12";
    private final String DID = "Z9X";
    private String code = "";
    private final String PSK = "0xa0,0xef,0x5d,0x13,0xf5,0x86,0x15,0x31,0xc3,0x89,0x36,0x9c,0xdf,0xa0,0x85,0xa7";
    private Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> players;

    protected Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> getPlayers() {
        return players;
    }


    public DmrQPlayService(LastChange lastChange, Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> players) {
        super(lastChange);
        this.players = players;
        Log.d(TAG, "DmrQPlayService: init");
    }

    @Override
    public ZxtMediaPlayer getInstance(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        ZxtMediaPlayer player = getPlayers().get(ZxtMediaPlayer.instanceId);
        if (player == null) {
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    @Override
    public QPlayAuth qPlayAuth(String seed) throws AVTransportException, NoSuchAlgorithmException {
        Log.d(TAG, "getQPlayAuth: " + seed);
        code = calculateCode(seed, PSK);
        getInstance(null).setqPlayAuth(code, MID, DID);
        return getInstance(null).getqPlayAuth();
    }

    @Override
    public String getTracksInfo(String startIndex, String number) throws ActionException {
        //● 功能：获取一个范围内的曲目信息。
        Log.d(TAG, "getTracksInfo: ");

        return null;
    }

    @Override
    public String getTracksCount() throws AVTransportException {
        //● 功能：返回QPlay队列中曲目的数量。
        Log.d(TAG, "getTracksCount: ");
        int num = getInstance(null).getTrackMetaData().getTracksMetaData().size();
        return String.valueOf(num);
    }


    /**
     * (in) StartingIndex - 在队列替换的起始位置(索引从1开始)。如果该值为-1，则整个队列被替换。
     * 	(in) NextIndex – 如果NextIndex是一个有效的队列位置，则旧队列的当前播放曲目保持不变，
     * 	播放完后下一个播放的曲目应该在NextIndex位置。如果NextIndex无效（NextIndex<1或者NextIndex>newPlaylist.length），
     * 	则当前播放的曲目应该立即停止播放。
     * 	(in) TracksMetaData - 队列曲目信息在TracksMetaData中定义。
     * 	(out) NumberOfSuccess - 成功设置的曲目数量。
     * */
    @Override
    public String setTracksInfo(String queueId, String startIndex, String nextIndex, String tracksMetaData) throws AVTransportException {
        //替换一个范围内的曲目列表
        String number = "0";
        if (tracksMetaData != null) {
            TrackMetaData trackMetaData = covertTrackData(tracksMetaData);
            Log.d(TAG, "setTracksInfo: " + trackMetaData);
            URI uri = null;
            try {
                uri = new URI(trackMetaData.getTracksMetaData().get(0).getTrackURIs().get(0));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (startIndex.equals("-1")){
                getInstance(null).setTrackMetaData(trackMetaData);
            }else {

            }
            int next = Integer.parseInt(nextIndex);
            if (next < 1 || next > trackMetaData.getTracksMetaData().size()) {
                getInstance(null).setQPlayURI(uri, "audio", trackMetaData.getTracksMetaData().get(0).getTitle(), tracksMetaData);
            }
        }
        return number;
    }

    @Override
    public String insertTracks(String queueId, String startIndex, String tracksMetaData) throws AVTransportException {
        //● 功能：在QPlay队列的指定位置开始插入一个或多个曲目
        Log.d(TAG, "insertTracks: ");
        if (tracksMetaData != null) {
            try {
                int startNum = getInstance(null).getTrackMetaData().getTracksMetaData().size();
                Log.d(TAG, "insertTracks: startNum" + startNum);
                TrackMetaData trackMetaData = covertTrackData(tracksMetaData);
                getInstance(null).getTrackMetaData().getTracksMetaData().addAll(Integer.parseInt(startIndex), trackMetaData.getTracksMetaData());
                int endNum = getInstance(null).getTrackMetaData().getTracksMetaData().size();
                Log.d(TAG, "insertTracks: endNum" + endNum);
                return String.valueOf(endNum-startNum);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "insertTracks: error" + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public String removeTracks(String queueId, String startIndex, String number) throws AVTransportException {
        //● 功能：在QPlay队列的指定位置开始移除一个或多个曲目
        Log.d(TAG, "removeTracks: ");
        int start = Integer.parseInt(startIndex);
        if (start < 0) {
            getInstance(null).getTrackMetaData().getTracksMetaData().clear();
            return "0";
        }
        int num = Integer.parseInt(number);
        int startNum = getInstance(null).getTrackMetaData().getTracksMetaData().size();
        List<TracksMetaData> list = getInstance(null).getTrackMetaData().getTracksMetaData();
        for (int i = 0; i < num; i++) {
            list.remove(start+i);
        }
        getInstance(null).getTrackMetaData().setTracksMetaData(list);
        int endNum = getInstance(null).getTrackMetaData().getTracksMetaData().size();
        return String.valueOf(endNum-startNum);
    }

    @Override
    public String getMaxTracks() throws ActionException {
        //● 功能：返回QPlay设备的队列最大支持曲目数量。
        Log.d(TAG, "getMaxTracks: ");
        return "500";
    }

    public static String calculateCode(String seed, String psk) throws NoSuchAlgorithmException {
        String input = seed + psk;

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }

        return sb.toString();
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[getPlayers().size()];
        int i = 0;
        for (UnsignedIntegerFourBytes id : getPlayers().keySet()) {
            ids[i] = id;
            i++;
        }
        return ids;
    }

    private TrackMetaData covertTrackData(String tracksMetaData) {
        if (tracksMetaData != null) {
            Gson gson = new Gson();
            TrackMetaData trackMetaData = gson.fromJson(tracksMetaData, TrackMetaData.class);
            return trackMetaData;
        }
        return null;
    }


}
