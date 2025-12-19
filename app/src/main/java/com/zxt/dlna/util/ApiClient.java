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
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:9529/ZidooMusicControl/v2";
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
        // 如果baseUrl已经包含/ZidooMusicControl/v2，则直接使用，否则自动拼接
        if (baseUrl != null && !baseUrl.endsWith("/ZidooMusicControl/v2")) {
            this.baseUrl = baseUrl + "/ZidooMusicControl/v2";
        } else {
            this.baseUrl = baseUrl;
        }
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
     * 获取单曲列表
     * 接口地址：/getSingleMusics
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getSingleMusics(Map<String, String> params, final ApiCallback<MusicResponse> callback) {
        String url = "/getSingleMusics";
        get(url, params, MusicResponse.class, callback);
    }
    
    /**
     * 获取艺术家列表接口（直接返回ArtistResponse对象）
     * 接口地址：/getArtists
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getArtists(Map<String, String> params, final ApiCallback<ArtistResponse> callback) {
        String url = "/getArtists";
        get(url, params, ArtistResponse.class, callback);
    }
    
    /**
     * 获取专辑列表接口（直接返回AlbumResponse对象）
     * 接口地址：/getAlbums
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getAlbums(Map<String, String> params, final ApiCallback<AlbumResponse> callback) {
        String url = "/getAlbums";
        get(url, params, AlbumResponse.class, callback);
    }

    /**
     * 获取专辑下的歌曲列表
     * 接口地址：/getAlbumMusics
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getAlbumMusics(Map<String, String> params, final ApiCallback<MusicResponse> callback) {
        String url = "/getAlbumMusics";
        // 使用MusicResponse类作为响应类型，因为它的结构符合要求（array为歌曲列表）
        get(url, params, MusicResponse.class, callback);
    }

    /**
     * 获取作曲家列表接口（直接返回ComposerResponse对象）
     * 接口地址：/getComposerList
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getComposerList(Map<String, String> params, final ApiCallback<ComposerResponse> callback) {
        String url = "/getComposerList";
        get(url, params, ComposerResponse.class, callback);
    }

    /**
     * 获取艺术家的专辑列表接口（直接返回AlbumResponse对象）
     * 接口地址：/getArtistAlbums
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getArtistAlbums(Map<String, String> params, final ApiCallback<AlbumResponse> callback) {
        String url = "/getArtistAlbums";
        get(url, params, AlbumResponse.class, callback);
    }
    
    /**
     * 获取艺术家的歌曲列表接口（直接返回MusicResponse对象）
     * 接口地址：/getArtistMusics
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getArtistMusics(Map<String, String> params, final ApiCallback<MusicResponse> callback) {
        String url = "/getArtistMusics";
        get(url, params, MusicResponse.class, callback);
    }
    
    /**
     * 获取作曲家的专辑列表接口（直接返回AlbumResponse对象）
     * 接口地址：/getComposerAlbumList
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getComposerAlbumList(Map<String, String> params, final ApiCallback<AlbumResponse> callback) {
        String url = "/getComposerAlbumList";
        get(url, params, AlbumResponse.class, callback);
    }

    /**
     * 获取作曲家的单曲列表接口（直接返回MusicResponse对象）
     * 接口地址：/getComposerAudioList
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getComposerAudioList(Map<String, String> params, final ApiCallback<MusicResponse> callback) {
        String url = "/getComposerAudioList";
        get(url, params, MusicResponse.class, callback);
    }
    
    /**
     * 获取流派的专辑列表接口（直接返回AlbumResponse对象）
     * 接口地址：/getGenreAlbumList
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getGenreAlbumList(Map<String, String> params, final ApiCallback<AlbumResponse> callback) {
        String url = "/getGenreAlbumList";
        get(url, params, AlbumResponse.class, callback);
    }

    /**
     * 获取过滤列表（包含流派等信息）
     * 接口地址：/getSingleFilterList
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void getSingleFilterList(Map<String, String> params, final ApiCallback<List<GenreResponse.FilterItem>> callback) {
        String url = "/getSingleFilterList";
        // 由于返回的是数组，使用Object.class作为响应类型，后续在回调中进行类型转换
        get(url, params, Object.class, new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object response) {
                // 响应是一个数组，需要进行转换
                List<GenreResponse.FilterItem> filterItems = new ArrayList<>();
                if (response instanceof List) {
                    for (Object item : (List<?>) response) {
                        if (item instanceof Map) {
                            // 手动解析Map为FilterItem
                            GenreResponse.FilterItem filterItem = new GenreResponse.FilterItem();
                            Map<?, ?> itemMap = (Map<?, ?>) item;
                            
                            // 解析key和name
                            if (itemMap.containsKey("key")) {
                                filterItem.setKey(String.valueOf(itemMap.get("key")));
                            }
                            if (itemMap.containsKey("name")) {
                                filterItem.setName(String.valueOf(itemMap.get("name")));
                            }
                            
                            // 解析data字段
                            if (itemMap.containsKey("data")) {
                                Object dataObj = itemMap.get("data");
                                if (dataObj instanceof List) {
                                    List<GenreResponse.GenreData> genreDataList = new ArrayList<>();
                                    for (Object dataItem : (List<?>) dataObj) {
                                        if (dataItem instanceof Map) {
                                            GenreResponse.GenreData genreData = new GenreResponse.GenreData();
                                            Map<?, ?> dataMap = (Map<?, ?>) dataItem;
                                            
                                            // 解析genre data字段
                                            if (dataMap.containsKey("typeID")) {
                                                Object typeIdObj = dataMap.get("typeID");
                                                if (typeIdObj instanceof Number) {
                                                    genreData.setTypeID(((Number) typeIdObj).intValue());
                                                } else {
                                                    try {
                                                        // 处理可能的浮点数字符串，如"1.0"
                                                        double typeIdDouble = Double.parseDouble(String.valueOf(typeIdObj));
                                                        genreData.setTypeID((int) typeIdDouble);
                                                    } catch (NumberFormatException e) {
                                                        // 如果解析失败，使用默认值0
                                                        genreData.setTypeID(0);
                                                    }
                                                }
                                            }
                                            if (dataMap.containsKey("name")) {
                                                genreData.setName(String.valueOf(dataMap.get("name")));
                                            }
                                            if (dataMap.containsKey("realName")) {
                                                genreData.setRealName(String.valueOf(dataMap.get("realName")));
                                            }
                                            if (dataMap.containsKey("genreImage")) {
                                                genreData.setGenreImage(String.valueOf(dataMap.get("genreImage")));
                                            }
                                            if (dataMap.containsKey("updateTime")) {
                                                Object updateTimeObj = dataMap.get("updateTime");
                                                if (updateTimeObj instanceof Number) {
                                                    genreData.setUpdateTime(((Number) updateTimeObj).longValue());
                                                } else {
                                                    try {
                                                        // 处理可能的浮点数字符串，如"1588492800000.0"
                                                        double updateTimeDouble = Double.parseDouble(String.valueOf(updateTimeObj));
                                                        genreData.setUpdateTime((long) updateTimeDouble);
                                                    } catch (NumberFormatException e) {
                                                        // 如果解析失败，使用默认值0
                                                        genreData.setUpdateTime(0);
                                                    }
                                                }
                                            }
                                            
                                            genreDataList.add(genreData);
                                        }
                                    }
                                    filterItem.setData(genreDataList);
                                }
                            }
                            
                            filterItems.add(filterItem);
                        }
                    }
                }
                
                callback.onSuccess(filterItems);
            }
            
            @Override
            public void onFailure(String errorMsg) {
                callback.onFailure(errorMsg);
            }
        });
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
     * 流派响应类，用于解析/ZidooMusicControl/v2/getSingleFilterList接口返回的流派数据
     */
    public static class GenreResponse {
        private List<FilterItem> data = new ArrayList<>();

        public List<FilterItem> getData() {
            return data;
        }

        public void setData(List<FilterItem> data) {
            this.data = data;
        }

        /**
         * 过滤项类
         */
        public static class FilterItem {
            private String key;
            private String name;
            private List<GenreData> data = new ArrayList<>();

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<GenreData> getData() {
                return data;
            }

            public void setData(List<GenreData> data) {
                this.data = data;
            }
        }

        /**
         * 流派数据类
         */
        public static class GenreData {
            private int typeID;
            private String name;
            private String realName;
            private String genreImage;
            private long updateTime;

            public int getTypeID() {
                return typeID;
            }

            public void setTypeID(int typeID) {
                this.typeID = typeID;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getRealName() {
                return realName;
            }

            public void setRealName(String realName) {
                this.realName = realName;
            }

            public String getGenreImage() {
                return genreImage;
            }

            public void setGenreImage(String genreImage) {
                this.genreImage = genreImage;
            }

            public long getUpdateTime() {
                return updateTime;
            }

            public void setUpdateTime(long updateTime) {
                this.updateTime = updateTime;
            }
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