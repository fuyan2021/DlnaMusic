package com.example.play;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketServer {
    private static final int PORT = 60002;
    private static final String TAG = "WebSocketServer";
    private WebSocket webSocket;

    public WebSocketServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://localhost:" + PORT).build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                sendMessage("server open");
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                // 处理接收到的消息
                Log.d(TAG, "onMessage: " + text);
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
                // 连接关闭时的处理
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, okhttp3.Response response) {
                super.onFailure(webSocket, t, response);
                // 连接失败时的处理
                Log.d(TAG, "onFailure: "+t.getMessage());
            }
        };

        webSocket = client.newWebSocket(request, listener);
    }

    public void sendMessage(String message) {
        webSocket.send(message);
    }
}
