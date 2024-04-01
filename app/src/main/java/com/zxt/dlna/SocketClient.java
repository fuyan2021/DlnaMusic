package com.zxt.dlna;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;

public class SocketClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static String TAG = "SocketClient";

    public void connectToServer() {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                try {
                    if (socket == null) {
                        socket = new Socket("127.0.0.1", 60003);
                    }
                    // 在这里可以进行数据的读取和写入操作
                    // 获取输入流和输出流
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    // 发送数据
                    sendMessage("{\n" +
                            "\"TracksMetaData\" :\n" +
                            "[\n" +
                            "{\n" +
                            "\"trackURIs\" :\n" +
                            "[\n" +
                            "\"http://tsmusic128.tc.qq.com/XlFNM15yAAAANQAAAP1zF+CzijEzJDk2Mu82NDMxQSTV7LoIAVkJB2wJ\n" +
                            "GVlDg24c5bKJOjQCPj9cxzc0Mw==/32547801.mp3?uid=2354092201&ct=0&chid=0&stream_pos=5\",\n" +
                            " \n" +
                            "\"http://tsmusic128.tc.qq.com/XlFNM15yAAAANQAAAP1zF+CzijEzJDk2Mu82NDMxQSTV7LoIAVkJB2wJ\n" +
                            "GVlDg24c5bKJOjQCPj9cxzc0Mw==/32547801.mp3?uid=2354092201&ct=0&chid=0&stream_pos=5\"\n" +
                            "],\n" +
                            "\"title\" : \"Just Give Me a Reason (feat. Nate Ruess)\",\n" +
                            "\"creator\" : \"P!nk\",\n" +
                            "\"album\" : \"The Truth About Love (Fan Edition)\",\n" +
                            "\"albumArtURI\" : \n" +
                            "\"http://imgcache.qq.com/music/photo/album_500/92/500_albumpic_198692_0.jpg\",\n" +
                            "\"songID\" : \"2547801\",\n" +
                            "\"duration\" : \"00:04:02\",\n" +
                            "\"protocolInfo\" : \"http\u0002get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;\",\n" +
                            "\"lyric\" : \"\",\n" +
                            "\"tag\" : \"\"\n" +
                            "},\n" +
                            "{\n" +
                            "\"trackURIs\" :\n" +
                            "[\n" +
                            "\"http://tsmusic128.tc.qq.com/XlFNM15LAAAAjwAAAOMRZXDgizEzFDk2MuI2NDOOI1ZFG7sIAVMJB2yl\n" +
                            "GVlDhwxjdNOIOTjOMTJd3zc0Mw==/31938518.mp3?uid=2354092201&ct=0&chid=0&stream_pos=5\",\n" +
                            " \n" +
                            "\"http://tsmusic128.tc.qq.com/XlFNM15LAAAAjwAAAOMRZXDgizEzFDk2MuI2NDOOI1ZFG7sIAVMJB2yl\n" +
                            "GVlDhwxjdNOIOTjOMTJd3zc0Mw==/31938518.mp3?uid=2354092201&ct=0&chid=0&stream_pos=5\"\n" +
                            "],\n" +
                            "\"title\" : \"Can't Hold Us (feat. Ray Dalton)\",\n" +
                            "\"creator\" : \"Macklemore & Ryan Lewis\",\n" +
                            "\"album\" : \"The Heist (Deluxe Versin)\",\n" +
                            "\"albumArtURI\" : \"http://imgcache.qq.com/music/photo/album/QPlay.jpg\",\n" +
                            "\"songID\" : \"1938518\",\n" +
                            "\"duration\" : \"00:04:18\",\n" +
                            "\"protocolInfo\" : \"http\u0002get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;\",\n" +
                            "\"lyric\" : \"\",\n" +
                            "\"tag\" : \"\"\n" +
                            "}\n" +
                            "]\n" +
                            "}");
                    String message;
                    while ((message = in.readLine()) != null) {
                        // 在这里处理接收到的消息
                        handleMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendMessage(final String message) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // 如果输出流未关闭，则发送消息
                if (out != null) {
                    try {
                        Log.d(TAG, "run: " + message);
                        Util.sendData(message.getBytes(), socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // 如果输出流已关闭，则重新连接并发送消息
                    connectToServer();
                    if (out != null) {
                        try {
                            Util.sendData(message.getBytes(), socket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
    }


    private void handleMessage(String message) {
        // 处理接收到的消息
        Log.d(TAG, "handleMessage: ");
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
