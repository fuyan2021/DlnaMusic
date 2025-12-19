package com.zxt.dlna.util;

import com.zxt.dlna.dms.bean.AlbumInfo;
import com.zxt.dlna.dms.bean.AudioInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 专辑容器类，用于管理从getAlbums接口获取的专辑列表
 */
public class AlbumContainer {
    
    private ApiClient.AlbumResponse albumResponse;
    private List<AlbumInfo> albumInfoList;
    private boolean isLoading;
    private String errorMessage;
    
    /**
     * 构造函数
     */
    public AlbumContainer() {
        this.albumInfoList = new ArrayList<>();
        this.isLoading = false;
    }
    
    /**
     * 获取专辑列表
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void loadAlbumList(Map<String, String> params, final LoadCallback callback) {
        if (isLoading) {
            if (callback != null) {
                callback.onFailure("正在加载中");
            }
            return;
        }
        
        isLoading = true;
        errorMessage = null;
        
        ApiClient apiClient = ApiClient.getInstance();
        apiClient.getAlbums(params, new ApiClient.ApiCallback<ApiClient.AlbumResponse>() {
            @Override
            public void onSuccess(ApiClient.AlbumResponse response) {
                albumResponse = response;
                albumInfoList = response != null ? response.getArray() : new ArrayList<AlbumInfo>();
                isLoading = false;
                
                // 为每个专辑获取歌曲列表
                if (albumInfoList != null && !albumInfoList.isEmpty()) {
                    loadSongsForAlbums(albumInfoList, callback);
                } else {
                    if (callback != null) {
                        callback.onSuccess(albumInfoList);
                    }
                }
            }
            
            @Override
            public void onFailure(String errorMsg) {
                errorMessage = errorMsg;
                isLoading = false;
                
                if (callback != null) {
                    callback.onFailure(errorMsg);
                }
            }
        });
    }
    
    /**
     * 获取专辑响应
     * @return 专辑响应
     */
    public ApiClient.AlbumResponse getAlbumResponse() {
        return albumResponse;
    }
    
    /**
     * 获取专辑列表
     * @return 专辑列表
     */
    public List<AlbumInfo> getAlbumInfoList() {
        return albumInfoList;
    }
    
    /**
     * 为多个专辑获取歌曲列表
     * @param albumList 专辑列表
     * @param callback 回调接口
     */
    private void loadSongsForAlbums(List<AlbumInfo> albumList, final LoadCallback callback) {
        if (albumList == null || albumList.isEmpty()) {
            if (callback != null) {
                callback.onSuccess(albumList);
            }
            return;
        }
        
        final int totalAlbums = albumList.size();
        final AtomicInteger loadedCount = new AtomicInteger(0);
        final ApiClient apiClient = ApiClient.getInstance();
        
        for (final AlbumInfo albumInfo : albumList) {
            Map<String, String> params = new HashMap<>();
            params.put("id", String.valueOf(albumInfo.getId()));
            params.put("start", "0");
            params.put("count", "100");
            
            apiClient.getAlbumMusics(params, new ApiClient.ApiCallback<ApiClient.MusicResponse>() {
                @Override
                public void onSuccess(ApiClient.MusicResponse response) {
                    List<AudioInfo> songs = response != null ? response.getArray() : new ArrayList<AudioInfo>();
                    albumInfo.setSongs(songs);
                    
                    // 检查是否所有专辑的歌曲都已加载完成
                    if (loadedCount.incrementAndGet() == totalAlbums) {
                        if (callback != null) {
                            callback.onSuccess(albumList);
                        }
                    }
                }
                
                @Override
                public void onFailure(String errorMsg) {
                    // 即使某个专辑的歌曲加载失败，也要继续加载其他专辑的歌曲
                    albumInfo.setSongs(new ArrayList<AudioInfo>());
                    
                    // 检查是否所有专辑的歌曲都已加载完成
                    if (loadedCount.incrementAndGet() == totalAlbums) {
                        if (callback != null) {
                            callback.onSuccess(albumList);
                        }
                    }
                }
            });
        }
    }
    
    /**
     * 是否正在加载
     * @return 是否正在加载
     */
    public boolean isLoading() {
        return isLoading;
    }
    
    /**
     * 获取错误信息
     * @return 错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 加载回调接口
     */
    public interface LoadCallback {
        void onSuccess(List<AlbumInfo> albumList);
        void onFailure(String errorMsg);
    }
}