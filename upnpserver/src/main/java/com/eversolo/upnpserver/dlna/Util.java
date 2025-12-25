package com.eversolo.upnpserver.dlna;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Description:  <br>
 *
 * @author: fy <br>
 * Date: 2024/3/29 <br>
 */
public class Util {
    private static final int PACKET_SIZE = 1024; // 每个包的大小

    public static byte[] receiveData(Socket socket) throws IOException {
        byte[] receivedData = null;

        // 获取输入流
        try (InputStream inputStream = socket.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream)) {

            // 读取包的数量
            int numPackets = dataInputStream.readInt();
            // 接收数据
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
        }
        return receivedData;
    }

    public static void sendData(byte[] data, Socket socket) throws IOException {
        // 建立与接收端的连接
        if (socket.isClosed()){
            socket = new Socket("127.0.0.1", 60003);
        }
        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        int dataSize = data.length;
        int numPackets = (int) Math.ceil((double) dataSize / PACKET_SIZE); // 计算需要分割的包数
        Log.d("SocketClient", "sendData: "+ numPackets);
        // 发送包的数量
        dataOutputStream.writeInt(numPackets);

        // 分割数据并发送每个包
        for (int i = 0; i < numPackets; i++) {
            int offset = i * PACKET_SIZE;
            int length = Math.min(dataSize - offset, PACKET_SIZE);
            dataOutputStream.writeInt(length); // 发送每个包的长度
            dataOutputStream.write(data, offset, length); // 发送每个包的数据
            dataOutputStream.flush();
        }
    }



    /**
     * &
     * socket分包
     */
    public static void sendPacket(OutputStream outputStream, String message) throws IOException {
        // 在消息前面添加消息长度作为分隔符
        byte[] messageBytes = message.getBytes();
        int messageLength = messageBytes.length;
        byte[] lengthBytes = intToByteArray(messageLength);

        // 发送消息长度
        outputStream.write(lengthBytes);

        // 发送消息内容
        outputStream.write(messageBytes);
        outputStream.flush();
    }

    private static byte[] intToByteArray(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte) (value >> 24);
        byteArray[1] = (byte) (value >> 16);
        byteArray[2] = (byte) (value >> 8);
        byteArray[3] = (byte) value;
        return byteArray;
    }

    public static String receivePacket(InputStream inputStream) throws IOException {
        byte[] lengthBytes = new byte[4];
        inputStream.read(lengthBytes);
        int messageLength = byteArrayToInt(lengthBytes);

        byte[] messageBytes = new byte[messageLength];
        inputStream.read(messageBytes);
        String message = new String(messageBytes);
        inputStream.close();
        return message;
    }

    private static int byteArrayToInt(byte[] byteArray) {
        return (byteArray[0] << 24) | ((byteArray[1] & 0xFF) << 16) | ((byteArray[2] & 0xFF) << 8) | (byteArray[3] & 0xFF);
    }
}
