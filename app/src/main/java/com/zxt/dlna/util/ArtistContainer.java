package com.zxt.dlna.util;

import com.zxt.dlna.dms.bean.ArtistInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 艺术家容器类，用于管理从getArtists接口获取的艺术家列表
 */
public class ArtistContainer {
    
    private ApiClient.ArtistResponse artistResponse;
    private List<ArtistInfo> artistInfoList;
    private boolean isLoading;
    private String errorMessage;
    
    /**
     * 构造函数
     */
    public ArtistContainer() {
        this.artistInfoList = new ArrayList<>();
        this.isLoading = false;
    }

    /**
     * 获取艺术家列表
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void loadArtistList(Map<String, String> params, final LoadCallback callback) {
        if (isLoading) {
            if (callback != null) {
                callback.onFailure("正在加载中");
            }
            return;
        }
        
        isLoading = true;
        errorMessage = null;
        
        ApiClient apiClient = ApiClient.getInstance();
        apiClient.getArtists(params, new ApiClient.ApiCallback<ApiClient.ArtistResponse>() {
            @Override
            public void onSuccess(ApiClient.ArtistResponse response) {
                artistResponse = response;
                artistInfoList = response != null ? response.getArray() : new ArrayList<ArtistInfo>();
                isLoading = false;
                
                if (callback != null) {
                    callback.onSuccess(artistInfoList);
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
     * 获取所有艺术家信息列表
     * @return 艺术家信息列表
     */
    public List<ArtistInfo> getArtistInfoList() {
        return artistInfoList;
    }
    
    /**
     * 获取艺术家响应对象
     * @return 艺术家响应对象
     */
    public ApiClient.ArtistResponse getArtistResponse() {
        return artistResponse;
    }

    /**
     * 获取总艺术家数
     * @return 总艺术家数
     */
    public int getTotalCount() {
        return artistResponse != null ? artistResponse.getTotal() : 0;
    }
    
    /**
     * 获取当前页起始位置
     * @return 起始位置
     */
    public int getStart() {
        return artistResponse != null ? artistResponse.getStart() : 0;
    }

    /**
     * 获取当前页艺术家数
     * @return 当前页艺术家数
     */
    public int getCurrentCount() {
        return artistResponse != null ? artistResponse.getCount() : 0;
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
     * 是否加载失败
     * @return 是否加载失败
     */
    public boolean isError() {
        return errorMessage != null;
    }

    /**
     * 列表是否为空
     * @return 列表是否为空
     */
    public boolean isEmpty() {
        return artistInfoList == null || artistInfoList.isEmpty();
    }
    
    /**
     * 清空列表
     */
    public void clear() {
        if (artistInfoList != null) {
            artistInfoList.clear();
        }
        artistResponse = null;
        errorMessage = null;
    }

    /**
     * 获取指定位置的艺术家信息
     * @param position 位置
     * @return 艺术家信息
     */
    public ArtistInfo getArtistInfo(int position) {
        if (artistInfoList != null && position >= 0 && position < artistInfoList.size()) {
            return artistInfoList.get(position);
        }
        return null;
    }

    /**
     * 获取艺术家信息的数量
     * @return 艺术家信息的数量
     */
    public int size() {
        return artistInfoList != null ? artistInfoList.size() : 0;
    }
    
    /**
     * 加载回调接口
     */
    public interface LoadCallback {
        /**
         * 加载成功
         * @param artistInfoList 艺术家信息列表
         */
        void onSuccess(List<ArtistInfo> artistInfoList);
        
        /**
         * 加载失败
         * @param errorMsg 错误信息
         */
        void onFailure(String errorMsg);
    }
}