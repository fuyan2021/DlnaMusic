package com.zxt.dlna.util;

import com.zxt.dlna.dms.bean.AlbumInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                
                if (callback != null) {
                    callback.onSuccess(albumInfoList);
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