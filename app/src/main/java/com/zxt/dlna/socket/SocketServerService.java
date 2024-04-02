package com.zxt.dlna.socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.zidoo.clingapi.Actions;
import com.zidoo.clingapi.IDLNAListener;
import com.zidoo.clingapi.Util;
import com.zidoo.clingapi.data.TrackData;import com.zidoo.clingapi.socket.SocketBean;import com.zidoo.clingapi.socket.SocketType;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class SocketServerService extends Service {
    private final static String TAG = "SocketServerService";
    public static SocketServerService instance;
    private static IDLNAListener listener;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        Executors.newFixedThreadPool(8).execute(() -> {
            while (true) {
                try {
                    if (serverSocket == null) {
                        serverSocket = new ServerSocket(60003);
                    }
                    if (serverSocket.isClosed()) {
                        Log.d(TAG, "onStartCommand: isClosed");
                        return;
                    }
                    clientSocket = serverSocket.accept();
                    Log.d(TAG, "onStartCommand: " + clientSocket.getSendBufferSize());
                    if (clientSocket.isClosed()) {
                        clientSocket = new ServerSocket(60003).accept();
                    }
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    try {
                        byte[] receivedData = null;
                        int dataSize = 0;
                        DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                        int numPackets = dataInputStream.readInt();
                        for (int i = 0; i < numPackets; i++) {
                            Log.d(TAG, "run: 读取" + numPackets);
                            if (numPackets <= 0) {
                                Log.d(TAG, "run: " + clientSocket.getInputStream().read());
                                break;
                            }
                            int packetSize = dataInputStream.readInt(); // 读取每个包的长度
                            byte[] packetData = new byte[packetSize];
                            int bytesRead = dataInputStream.read(packetData); // 读取每个包的数据
                            dataSize += bytesRead;
                            if (receivedData == null) {
                                receivedData = packetData;
                            } else {
                                // 将数据包拼接到已接收的数据中
                                byte[] temp = new byte[receivedData.length + packetData.length];
                                System.arraycopy(receivedData, 0, temp, 0, receivedData.length);
                                System.arraycopy(packetData, 0, temp, receivedData.length, packetData.length);
                                receivedData = temp;
                            }
                        }
                        Log.d(TAG, "run: " + new String(receivedData));
                        handleMessage(new String(receivedData));
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    in.close();
//                    clientSocket.close();
//                    Log.d(TAG, "onStartCommand: closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    public static void setListener(IDLNAListener mlistener) {
        listener = mlistener;
    }

    private void receive() {

    }

    public void sendMessage(final String message) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 如果输出流未关闭，则发送消息
            if (out != null) {
                try {
                    Log.d(TAG, "run: " + message);
                    if (serverSocket == null || serverSocket.isClosed()) {
                        serverSocket = new ServerSocket(60003);
                    }
                    Util.sendData(message.getBytes(), serverSocket.accept());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void handleMessage(String data) {
        Gson gson = new Gson();
        try {
            if (data.contains(Util.HEART)) {
                Log.d(TAG, "handleMessage: " + data);
                return;
            }
            SocketBean socketBean = gson.fromJson(data, SocketBean.class);
            if (socketBean.getTrackMetaData() != null) {
                Log.d(TAG, "handleMessage: " + socketBean.getTrackMetaData().getTracksMetaData().size());
            }
            checkAction(socketBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        instance = null;
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void checkAction(SocketBean socketBean) {
        switch (socketBean.getActions()) {
            case Play:
                SocketType type = SocketType.valueOf(socketBean.getType());
                if (type.equals(SocketType.QPlay)) {
                    listener.qPlay(socketBean.getTrackMetaData());
                } else {
                    listener.dlnaPlay("", "");
                }
                break;
            case Pause:
                listener.pause();
                break;
            case Stop:
                listener.stop();
                break;
            case Seek:
                listener.onSeek("");
                break;
            case GetVolume:
                listener.getVolume();
                break;
            case SetVolume:
                listener.setVolume((int) Math.ceil(socketBean.getVolume()));
                break;
            default:
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
