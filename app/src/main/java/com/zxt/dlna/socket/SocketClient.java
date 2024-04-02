package com.zxt.dlna.socket;

import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;
import com.zidoo.clingapi.Actions;
import com.zidoo.clingapi.MediaListener;
import com.zidoo.clingapi.Util;import com.zidoo.clingapi.socket.SocketBean;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;

public class SocketClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static String TAG = "SocketClient";
    private static MediaListener mediaListener;
    private static final int HEARTBEAT_INTERVAL = 5000;
    // 心跳间隔时间
    private Handler heartHandler = new Handler();
    private Runnable heartRunnable = new Runnable() {
        @Override
        public void run() {
            heartHandler.removeCallbacks(this);
            sendMessage(Util.HEART);
            heartHandler.postDelayed(this, HEARTBEAT_INTERVAL);
        }
    };


    public void connectToServer() {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket == null) {
                        socket = new Socket("127.0.0.1", 60003);
                        heartHandler.post(heartRunnable);
                        sendMessage(new Gson().toJson(new SocketBean(Actions.GetVolume)));
                    }
                    String message;
                    while (true) {
                        out = new PrintWriter(socket.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        while ((message = in.readLine()) != null) {
                            // 在这里处理接收到的消息
                            handleMessage(message);
                        }
                        out.close();
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendMessage(final String message) {
        heartHandler.removeCallbacks(heartRunnable);
        heartHandler.postDelayed(heartRunnable,HEARTBEAT_INTERVAL);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // 如果输出流未关闭，则发送消息
                if (out != null) {
                    try {
                        if (socket.isClosed()){
                            return;
                        }
                        Log.d(TAG, "run: " + message);
                        Util.sendData(message.getBytes(), socket);
                    } catch (IOException e) {
                        e.printStackTrace();
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

    private void handleMessage(String message) throws IOException {
        // 处理接收到的消息
        Log.d(TAG, "handleMessage: " + message);
        heartHandler.post(heartRunnable);
        byte[] receivedData = null;
        while (in.readLine() != null) {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            int numPackets = dataInputStream.readInt();
            if (numPackets <= 0) {
                Log.d(TAG, "handleMessage: " + socket.getInputStream().read());
                return;
            }
            int dataSize = 0;
            for (int i = 0; i < numPackets; i++) {
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
            try {
                SocketBean socketBean = new Gson().fromJson(new String(receivedData), SocketBean.class);
                if (socketBean != null) {
                    checkAction(socketBean);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            receivedData = null;
            dataInputStream.close();
        }
    }

    private void checkAction(SocketBean socketBean) {
        Log.d(TAG, "checkAction: " + socketBean.getActions().name());
        switch (socketBean.getActions()) {
            case Next:
                mediaListener.next();
                break;
            case Play:
                mediaListener.start();
                break;
            case Seek:
                // TODO: 2024/4/2
                break;
            case Pause:
                mediaListener.pause();
                break;
            case Stop:
                mediaListener.stop();
                break;
            case Previous:
                // TODO: 2024/4/2
                break;
            case Record:
                // TODO: 2024/4/2
                break;
            case PROGRESS:
                mediaListener.positionChanged(socketBean.getPosition());
                mediaListener.durationChanged(socketBean.getDuration());
                break;
            case SetVolume:
            case GetVolume:
                mediaListener.getVolume(socketBean.getVolume());
            default:
                break;
        }
    }

    public void disconnect() {
        try {
            heartHandler.removeCallbacks(heartRunnable);
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

    public static void setMediaListener(MediaListener listener) {
        mediaListener = listener;
    }
}
