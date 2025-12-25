package com.eversolo.upnpserver.dlna.util;

import android.util.Log;

import com.eversolo.upnpserver.dlna.dms.bean.ComposerInfo;
import com.eversolo.upnpserver.dlna.util.ApiClient.ApiCallback;
import com.eversolo.upnpserver.dlna.util.ApiClient.ComposerResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作曲家容器，用于管理作曲家列表
 */
public class ComposerContainer {
    private static final String TAG = "ComposerContainer";
    private static final boolean DEBUG_API = false;

    private ComposerResponse composerResponse;
    private List<ComposerInfo> composerInfoList;
    private boolean isLoading;
    private String errorMessage;
    private LoadCallback loadCallback;

    /**
     * 构造函数
     */
    public ComposerContainer() {
        this.composerInfoList = new ArrayList<>();
        this.isLoading = false;
    }

    /**
     * 加载作曲家列表
     *
     * @param params   请求参数
     * @param callback 加载回调
     */
    public void loadComposerList(Map<String, String> params, final LoadCallback callback) {
        this.loadCallback = callback;

        if (isLoading) {
            if (callback != null) {
                callback.onFailure("正在加载中");
            }
            return;
        }

        isLoading = true;
        errorMessage = null;

        // 清空现有数据，准备重新加载
        composerInfoList.clear();

        if (DEBUG_API) {
            Log.d(TAG, "loadComposerList - params: " + params);
        }

        // 开始自动分页加载
        loadComposerListWithPagination(params, callback);
    }
    
    /**
     * 带自动分页的作曲家列表加载
     * @param params 请求参数
     * @param callback 回调接口
     */
    private void loadComposerListWithPagination(final Map<String, String> params, final LoadCallback callback) {
        ApiClient apiClient = ApiClient.getInstance();
        apiClient.getComposerList(params, new ApiCallback<ComposerResponse>() {
            @Override
            public void onSuccess(ComposerResponse response) {
                if (response != null && response.getArray() != null && !response.getArray().isEmpty()) {
                    // 将当前页数据添加到列表中
                    composerInfoList.addAll(response.getArray());
                    composerResponse = response;
                    
                    if (DEBUG_API) {
                        Log.d(TAG, "loadComposerListWithPagination - success, composer count: " + composerInfoList.size());
                    }
                    
                    // 检查是否还有更多数据需要加载
                    int currentStart = response.getStart();
                    int currentCount = response.getCount();
                    int totalCount = response.getTotal();
                    
                    if (currentStart + currentCount < totalCount) {
                        // 还有更多数据，继续加载下一页
                        Map<String, String> nextParams = new HashMap<>(params);
                        nextParams.put("start", String.valueOf(currentStart + currentCount));
                        nextParams.put("count", params.get("count")); // 保持相同的每页数量
                        
                        loadComposerListWithPagination(nextParams, callback);
                        return;
                    }
                }
                
                isLoading = false;
                
                if (callback != null) {
                    if (DEBUG_API) {
                        Log.d(TAG, "loadComposerListWithPagination - all pages loaded, total composer count: " + composerInfoList.size());
                    }
                    callback.onSuccess(composerInfoList);
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "loadComposerListWithPagination - failure: " + errorMsg);
                errorMessage = errorMsg;
                isLoading = false;
                if (callback != null) {
                    callback.onFailure(errorMsg);
                }
            }
        });
    }

    public ComposerResponse getComposerResponse() {
        return composerResponse;
    }

    public List<ComposerInfo> getComposerInfoList() {
        return composerInfoList;
    }

    public void setComposerInfoList(List<ComposerInfo> composerInfoList) {
        this.composerInfoList = composerInfoList;
    }

    /**
     * 加载回调接口
     */
    public interface LoadCallback {
        /**
         * 加载成功
         *
         * @param composerList 作曲家列表
         */
        void onSuccess(List<ComposerInfo> composerList);

        /**
         * 加载失败
         *
         * @param errorMsg 错误信息
         */
        void onFailure(String errorMsg);
    }
}