package com.zxt.dlna.util;

import android.util.Log;

import com.zxt.dlna.dms.bean.ComposerInfo;
import com.zxt.dlna.util.ApiClient.ApiCallback;
import com.zxt.dlna.util.ApiClient.ComposerResponse;

import java.util.ArrayList;
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

        if (DEBUG_API) {
            Log.d(TAG, "loadComposerList - params: " + params);
        }

        ApiClient apiClient = ApiClient.getInstance();
        apiClient.getComposerList(params, new ApiCallback<ComposerResponse>() {
            @Override
            public void onSuccess(ComposerResponse response) {
                composerResponse = response;
                composerInfoList = response != null ? response.getArray() : new ArrayList<ComposerInfo>();
                isLoading = false;

                if (DEBUG_API) {
                    Log.d(TAG, "loadComposerList - success, composer count: " + (composerInfoList != null ? composerInfoList.size() : 0));
                }

                if (callback != null) {
                    callback.onSuccess(composerInfoList);
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "loadComposerList - failure: " + errorMsg);
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