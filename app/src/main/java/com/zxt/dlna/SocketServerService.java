package com.zxt.dlna;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class SocketServerService extends Service {
    private final static String TAG = "SocketServerService";
    public static SocketServerService instance;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        Executors.newFixedThreadPool(8).execute(() -> {
            while (true) {
                try {
                    ServerSocket serverSocket = new ServerSocket(60003);
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    byte[] receivedData = null;
                    if (clientSocket.isClosed()) {
                        clientSocket = new ServerSocket(60001).accept();
                    }
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    int numPackets = dataInputStream.readInt();
                    if (numPackets <= 0) {
                        Log.d(TAG, "run: " + clientSocket.getInputStream().read());
                        return;
                    }
                    Log.d(TAG, "run: 读取" + numPackets);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }


    public void send(String json) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        instance = null;
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
