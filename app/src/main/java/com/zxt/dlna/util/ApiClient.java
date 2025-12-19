package com.zxt.dlna.util;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.zxt.dlna.dms.bean.AlbumInfo;
import com.zxt.dlna.dms.bean.AudioInfo;
import com.zxt.dlna.dms.bean.ArtistInfo;
import com.zxt.dlna.dms.bean.ComposerInfo;

import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp接口请求工具类
 */
public class ApiClient {

    // 默认基础URL为本机IP
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:9529";
    //base固定不变
    private String baseUrl;
    private OkHttpClient okHttpClient;

    /**
     * 单例模式
     */
    private static class SingletonHolder {
        private static final ApiClient INSTANCE = new ApiClient();
    }

    public static ApiClient getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 私有构造函数
     */
    private ApiClient() {
        this(DEFAULT_BASE_URL);
    }

    /**
     * 构造函数，可自定义基础URL
     * @param baseUrl 基础URL
     */
    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        
        // 初始化OkHttpClient
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 设置基础URL
     * @param baseUrl 基础URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 获取基础URL
     * @return 基础URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * GET请求（字符串响应）
     * @param url 接口地址（相对于baseUrl）
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void get(String url, Map<String, String> params, final StringCallback callback) {
        // 构建完整URL
        String fullUrl = buildUrl(url, params);
        
        Request request = new Request.Builder()
                .url(fullUrl)
                .get()
                .build();
        
        executeRequest(request, callback);
    }
    
    /**
     * GET请求（泛型响应）
     * @param url 接口地址（相对于baseUrl）
     * @param params 请求参数
     * @param clazz 响应类型
     * @param callback 回调接口
     */
    public <T> void get(String url, Map<String, String> params, Class<T> clazz, final ApiCallback<T> callback) {
        // 构建完整URL
        String fullUrl = buildUrl(url, params);
        
        Request request = new Request.Builder()
                .url(fullUrl)
                .get()
                .build();
        
        executeRequest(request, clazz, callback);
    }

    /**
     * 获取单曲音乐接口（直接返回MusicResponse对象）
     * 接口地址：/ZidooMusicControl/v2/getSingleMusics
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getSingleMusics(Map<String, String> params, final ApiCallback<MusicResponse> callback) {
        String url = "/ZidooMusicControl/v2/getSingleMusics";
        get(url, params, MusicResponse.class, callback);
    }
    
    /**
     * 获取艺术家列表接口（直接返回ArtistResponse对象）
     * 接口地址：/ZidooMusicControl/v2/getArtists
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getArtists(Map<String, String> params, final ApiCallback<ArtistResponse> callback) {
        String url = "/ZidooMusicControl/v2/getArtists";
        get(url, params, ArtistResponse.class, callback);
    }
    
    /**
     * 获取专辑列表接口（直接返回AlbumResponse对象）
     * 接口地址：/ZidooMusicControl/v2/getAlbums
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getAlbums(Map<String, String> params, final ApiCallback<AlbumResponse> callback) {
        String url = "/ZidooMusicControl/v2/getAlbums";
        get(url, params, AlbumResponse.class, callback);
    }

    /**
     * 获取作曲家列表接口（直接返回ComposerResponse对象）
     * 接口地址：/ZidooMusicControl/v2/getComposerList
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getComposerList(Map<String, String> params, final ApiCallback<ComposerResponse> callback) {
        String url = "/ZidooMusicControl/v2/getComposerList";
        get(url, params, ComposerResponse.class, callback);
    }

    /**
     * POST请求（表单形式，字符串响应）
     * @param url 接口地址（相对于baseUrl）
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void post(String url, Map<String, String> params, final StringCallback callback) {
        // 构建表单请求体
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody requestBody = formBodyBuilder.build();
        
        Request request = new Request.Builder()
                .url(baseUrl + url)
                .post(requestBody)
                .build();
        
        executeRequest(request, callback);
    }
    
    /**
     * POST请求（表单形式，泛型响应）
     * @param url 接口地址（相对于baseUrl）
     * @param params 请求参数
     * @param clazz 响应类型
     * @param callback 回调接口
     */
    public <T> void post(String url, Map<String, String> params, Class<T> clazz, final ApiCallback<T> callback) {
        // 构建表单请求体
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody requestBody = formBodyBuilder.build();
        
        Request request = new Request.Builder()
                .url(baseUrl + url)
                .post(requestBody)
                .build();
        
        executeRequest(request, clazz, callback);
    }

    /**
     * POST请求（JSON形式，字符串响应）
     * @param url 接口地址（相对于baseUrl）
     * @param json 请求JSON字符串
     * @param callback 回调接口
     */
    public void postJson(String url, String json, final StringCallback callback) {
        // 构建JSON请求体
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, json);
        
        Request request = new Request.Builder()
                .url(baseUrl + url)
                .post(requestBody)
                .build();
        
        executeRequest(request, callback);
    }
    
    /**
     * POST请求（JSON形式，泛型响应）
     * @param url 接口地址（相对于baseUrl）
     * @param json 请求JSON字符串
     * @param clazz 响应类型
     * @param callback 回调接口
     */
    public <T> void postJson(String url, String json, Class<T> clazz, final ApiCallback<T> callback) {
        // 构建JSON请求体
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, json);
        
        Request request = new Request.Builder()
                .url(baseUrl + url)
                .post(requestBody)
                .build();
        
        executeRequest(request, clazz, callback);
    }

    /**
     * 构建完整URL（包含参数）
     * @param url 接口地址
     * @param params 请求参数
     * @return 完整URL
     */
    private String buildUrl(String url, Map<String, String> params) {
        StringBuilder fullUrl = new StringBuilder(baseUrl + url);
        
        if (params != null && !params.isEmpty()) {
            fullUrl.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                fullUrl.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
            // 移除最后一个&符号
            fullUrl.deleteCharAt(fullUrl.length() - 1);
        }
        
        return fullUrl.toString();
    }

    // Gson实例
    private Gson gson = new Gson();
    
    /**
     * 执行请求（字符串响应）
     * @param request 请求对象
     * @param callback 回调接口
     */
    private void executeRequest(Request request, final StringCallback callback) {
        // 打印完整请求URL
        Log.d("ApiClient", "完整请求URL: " + request.url());
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (callback != null) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        callback.onSuccess(responseBody);
                    } else {
                        callback.onFailure("请求失败: " + response.code());
                    }
                }
            }
        });
    }
    
    /**
     * 执行请求（泛型响应）
     * @param request 请求对象
     * @param clazz 响应类型
     * @param callback 回调接口
     */
    private <T> void executeRequest(Request request, final Class<T> clazz, final ApiCallback<T> callback) {
        // 打印完整请求URL
        Log.d("ApiClient", "完整请求URL: " + request.url());
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (callback != null) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        // 打印原始接口响应数据
                        Log.d("ApiClient", "原始接口响应数据: " + responseBody);
                        try {
                            T responseObject = gson.fromJson(responseBody, clazz);
                            callback.onSuccess(responseObject);
                        } catch (Exception e) {
                            callback.onFailure("JSON解析失败: " + e.getMessage());
                        }
                    } else {
                        callback.onFailure("请求失败: " + response.code());
                    }
                }
            }
        });
    }

    /**
     * 音乐响应模型
     */
    public static class MusicResponse {
        private int id;
        private int start;
        private int count;
        private int total;
        private List<AudioInfo> array = new ArrayList<>();
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public int getStart() {
            return start;
        }
        
        public void setStart(int start) {
            this.start = start;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public List<AudioInfo> getArray() {
            return array;
        }
        
        public void setArray(List<AudioInfo> array) {
            this.array = array != null ? array : new ArrayList<>();
        }
    }
    
    /**
     * 艺术家响应模型
     */
    public static class ArtistResponse {
        private int id;
        private int start;
        private int count;
        private int total;
        private List<ArtistInfo> array = new ArrayList<>();
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public int getStart() {
            return start;
        }
        
        public void setStart(int start) {
            this.start = start;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public List<ArtistInfo> getArray() {
            return array;
        }
        
        public void setArray(List<ArtistInfo> array) {
            this.array = array != null ? array : new ArrayList<>();
        }
    }
    
    /**
     * 专辑响应模型
     */
    public static class AlbumResponse {
        private int id;
        private int start;
        private int count;
        private int total;
        private List<AlbumInfo> array = new ArrayList<>();
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public int getStart() {
            return start;
        }
        
        public void setStart(int start) {
            this.start = start;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public List<AlbumInfo> getArray() {
            return array;
        }
        
        public void setArray(List<AlbumInfo> array) {
            this.array = array != null ? array : new ArrayList<>();
        }
    }
    
    /**
     * 作曲家响应模型
     */
    public static class ComposerResponse {
        private int id;
        private int start;
        private int count;
        private int total;
        private List<ComposerInfo> array = new ArrayList<>();
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public int getStart() {
            return start;
        }
        
        public void setStart(int start) {
            this.start = start;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public List<ComposerInfo> getArray() {
            return array;
        }
        
        public void setArray(List<ComposerInfo> array) {
            this.array = array != null ? array : new ArrayList<>();
        }
    }


    /**
     * 接口请求回调（支持泛型）
     */
    public interface ApiCallback<T> {
        /**
         * 请求成功
         * @param response 响应数据
         */
        void onSuccess(T response);
        
        /**
         * 请求失败
         * @param errorMsg 错误信息
         */
        void onFailure(String errorMsg);
    }
    
    /**
     * 字符串响应回调（兼容旧版）
     */
    public interface StringCallback extends ApiCallback<String> {
    }
}