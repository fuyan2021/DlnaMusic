package com.eversolo.upnpserver.dlna.util;

import com.eversolo.upnpserver.dlna.dms.bean.AudioInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单曲容器类，用于管理从getSingleMusics接口获取的音乐列表
 */
public class SingleMusicContainer {
    
    private ApiClient.MusicResponse musicResponse;
    private List<AudioInfo> audioInfoList;
    private boolean isLoading;
    private String errorMessage;
    
    /**
     * 构造函数
     */
    public SingleMusicContainer() {
        this.audioInfoList = new ArrayList<>();
        this.isLoading = false;
    }
    
    /**
     * 获取音乐列表
     * @param params 请求参数
     * @param callback 回调接口
     */
    public void loadMusicList(Map<String, String> params, final LoadCallback callback) {
        if (isLoading) {
            if (callback != null) {
                callback.onFailure("正在加载中");
            }
            return;
        }
        
        isLoading = true;
        errorMessage = null;
        
        // 清空现有数据，准备重新加载
        audioInfoList.clear();
        
        // 开始自动分页加载
        loadMusicListWithPagination(params, callback);
    }
    
    /**
     * 带自动分页的音乐列表加载
     * @param params 请求参数
     * @param callback 回调接口
     */
    private void loadMusicListWithPagination(final Map<String, String> params, final LoadCallback callback) {
        ApiClient apiClient = ApiClient.getInstance();
        apiClient.getSingleMusics(params, new ApiClient.ApiCallback<ApiClient.MusicResponse>() {
            @Override
            public void onSuccess(ApiClient.MusicResponse response) {
                if (response != null && response.getArray() != null && !response.getArray().isEmpty()) {
                    // 将当前页数据添加到列表中
                    audioInfoList.addAll(response.getArray());
                    musicResponse = response;
                    
                    // 检查是否还有更多数据需要加载
                    int currentStart = response.getStart();
                    int currentCount = response.getCount();
                    int totalCount = response.getTotal();
                    
                    if (currentStart + currentCount < totalCount) {
                        // 还有更多数据，继续加载下一页
                        Map<String, String> nextParams = new HashMap<>(params);
                        nextParams.put("start", String.valueOf(currentStart + currentCount));
                        nextParams.put("count", params.get("count")); // 保持相同的每页数量
                        
                        loadMusicListWithPagination(nextParams, callback);
                        return;
                    }
                }
                
                isLoading = false;
                
                if (callback != null) {
                    callback.onSuccess(audioInfoList);
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
     * 获取所有音频信息列表
     * @return 音频信息列表
     */
    public List<AudioInfo> getAudioInfoList() {
        return audioInfoList;
    }
    
    /**
     * 获取音乐响应对象
     * @return 音乐响应对象
     */
    public ApiClient.MusicResponse getMusicResponse() {
        return musicResponse;
    }
    
    /**
     * 获取总音乐数
     * @return 总音乐数
     */
    public int getTotalCount() {
        return musicResponse != null ? musicResponse.getTotal() : 0;
    }
    
    /**
     * 获取当前页起始位置
     * @return 起始位置
     */
    public int getStart() {
        return musicResponse != null ? musicResponse.getStart() : 0;
    }
    
    /**
     * 获取当前页音乐数
     * @return 当前页音乐数
     */
    public int getCurrentCount() {
        return musicResponse != null ? musicResponse.getCount() : 0;
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
        return audioInfoList == null || audioInfoList.isEmpty();
    }
    
    /**
     * 清空列表
     */
    public void clear() {
        if (audioInfoList != null) {
            audioInfoList.clear();
        }
        musicResponse = null;
        errorMessage = null;
    }
    
    /**
     * 获取指定位置的音频信息
     * @param position 位置
     * @return 音频信息
     */
    public AudioInfo getAudioInfo(int position) {
        if (audioInfoList != null && position >= 0 && position < audioInfoList.size()) {
            return audioInfoList.get(position);
        }
        return null;
    }
    
    /**
     * 获取音频信息的数量
     * @return 音频信息的数量
     */
    public int size() {
        return audioInfoList != null ? audioInfoList.size() : 0;
    }
    
    /**
     * 加载回调接口
     */
    public interface LoadCallback {
        /**
         * 加载成功
         * @param audioInfoList 音频信息列表
         */
        void onSuccess(List<AudioInfo> audioInfoList);
        
        /**
         * 加载失败
         * @param errorMsg 错误信息
         */
        void onFailure(String errorMsg);
    }
}