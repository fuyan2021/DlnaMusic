package org.fourthline.cling.support.qplay;

import android.util.Log;

import com.google.gson.Gson;
import com.zxt.dlna.dmr.ZxtMediaPlayer;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.lastchange.LastChange;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description: QPlay服务具体实现 <br>
 *
 * @author: fy <br>
 * Date: 2024/2/20 <br>
 */
public class DmrQPlayService extends AbstractQPlayService {
    private final String TAG = "DmrQPlayService";
    private final String MID = "12";
    private final String DID = "DMP-A6";
    private String code = "";
    private final String PSK = "0xa0,0xef,0x5d,0x13,0xf5,0x86,0x15,0x31,0xc3,0x89,0x36,0x9c,0xdf,0xa0,0x85,0xa7";
    private  Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> players;
    protected Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> getPlayers() {
        return players;
    }
    protected ZxtMediaPlayer getInstance() throws AVTransportException {
        ZxtMediaPlayer player = getPlayers().get(ZxtMediaPlayer.instanceId);
        if (player == null) {
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    public DmrQPlayService( Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> players) {
        super();
        this.players = players;
        Log.d(TAG, "DmrQPlayService: init");
    }

    @Override
    public QPlayAuth qPlayAuth(String seed) throws AVTransportException, NoSuchAlgorithmException {
        Log.d(TAG, "getQPlayAuth: "+seed);
        // TODO: 2024/2/20 扬声器返回的结果有：制造商ID（MID）、设备类型ID（DID）、
        //  随机字符串（Seed）与预共享密钥（PSK）相结合的哈希值（计算方式为：Code = MD5[Seed + PSK]）。
        code = calculateCode(seed,PSK);
        QPlayAuth auth = new QPlayAuth(code,MID,DID);
        Gson gson = new Gson();
        String result = gson.toJson(auth);
        return auth;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDid() {
        return this.DID;
    }

    @Override
    public String getMid() {
        return this.MID;
    }

    @Override
    public String getTracksInfo(String startIndex, String number) throws ActionException {
        Log.d(TAG, "getTracksInfo: ");
        return null;
    }

    @Override
    public String getTracksCount() throws AVTransportException {
        Log.d(TAG, "getTracksCount: ");
        return getInstance().getTrackMetaData().getTracksMetaData().size()+"";
    }

    @Override
    public String setTracksInfo(String queueId, String startIndex, String nextIndex, String tracksMetaData) throws AVTransportException{
        String number = "0";
        if (tracksMetaData!=null){
            Gson gson = new Gson();
            TrackMetaData trackMetaData = gson.fromJson(tracksMetaData, TrackMetaData.class);
            Log.d(TAG, "setTracksInfo: "+trackMetaData);
            URI uri = null;
            try {
                uri = new URI(trackMetaData.getTracksMetaData().get(0).getTrackURIs().get(0));
            } catch (Exception ex) {
               ex.printStackTrace();
            }
            getInstance().setQPlayURI(uri,"audio",trackMetaData.getTracksMetaData().get(0).getTitle(),tracksMetaData);
        }
        return number;
    }

    @Override
    public String insertTracks(String queueId, String startIndex, String tracksMetaData) throws AVTransportException {
        Log.d(TAG, "insertTracks: ");
        return null;
    }

    @Override
    public String removeTracks(String queueId, String startIndex, String number) throws AVTransportException {
        Log.d(TAG, "removeTracks: ");
        return null;
    }

    @Override
    public String getMaxTracks() throws ActionException {
        Log.d(TAG, "getMaxTracks: ");
        return getInstance().getMaxTracks();
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

//    @Override
//    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
//        return new UnsignedIntegerFourBytes[0];
//    }
}
