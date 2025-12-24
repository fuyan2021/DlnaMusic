package com.zxt.dlna.util;

import android.util.Log;

import com.zxt.dlna.dms.bean.GenreInfo;
import com.zxt.dlna.util.ApiClient.ApiCallback;
import com.zxt.dlna.util.ApiClient.GenreResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 流派容器，用于管理从getSingleFilterList接口获取的流派列表
 */
public class GenreContainer {
    private static final String TAG = "GenreContainer";
    private static final boolean DEBUG_API = false;

    private List<GenreInfo> genreInfoList;
    private boolean isLoading;
    private String errorMessage;
    private LoadCallback loadCallback;

    /**
     * 构造函数
     */
    public GenreContainer() {
        this.genreInfoList = new ArrayList<>();
        this.isLoading = false;
    }

    /**
     * 加载流派列表
     *
     * @param params      请求参数
     * @param callback    加载回调
     */
    public void loadGenreList(Map<String, String> params, final LoadCallback callback) {
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
            Log.d(TAG, "loadGenreList - params: " + params);
        }

        ApiClient apiClient = ApiClient.getInstance();
        apiClient.getSingleFilterList(params, new ApiCallback<List<GenreResponse.FilterItem>>() {
            @Override
            public void onSuccess(List<GenreResponse.FilterItem> filterItems) {
                genreInfoList = new ArrayList<>();
                isLoading = false;

                if (DEBUG_API) {
                    Log.d(TAG, "loadGenreList - success, filter item count: " + filterItems.size());
                }

                // 只处理key为"genres"的项
                for (GenreResponse.FilterItem filterItem : filterItems) {
                    if ("genres".equals(filterItem.getKey())) {
                        // 解析流派数据
                        List<GenreResponse.GenreData> genreDataList = filterItem.getData();
                        if (genreDataList != null && !genreDataList.isEmpty()) {
                            for (GenreResponse.GenreData genreData : genreDataList) {
                                GenreInfo genreInfo = new GenreInfo();
                                genreInfo.setGenreId(genreData.getTypeID());
                                genreInfo.setName(genreData.getName());
                                genreInfo.setDisplayName(genreData.getRealName());
                                genreInfo.setGenreImage(genreData.getGenreImage());
                                
                                genreInfoList.add(genreInfo);
                            }
                        }
                        break; // 找到genres项后退出循环
                    }
                }

                if (DEBUG_API) {
                    Log.d(TAG, "loadGenreList - 解析后流派数量: " + genreInfoList.size());
                }

                if (callback != null) {
                    callback.onSuccess(genreInfoList);
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "loadGenreList - failure: " + errorMsg);
                errorMessage = errorMsg;
                isLoading = false;
                if (callback != null) {
                    callback.onFailure(errorMsg);
                }
            }
        });
    }

    public List<GenreInfo> getGenreInfoList() {
        return genreInfoList;
    }

    public void setGenreInfoList(List<GenreInfo> genreInfoList) {
        this.genreInfoList = genreInfoList;
    }
    
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * 加载回调接口
     */
    public interface LoadCallback {
        /**
         * 加载成功
         *
         * @param genreList 流派列表
         */
        void onSuccess(List<GenreInfo> genreList);

        /**
         * 加载失败
         *
         * @param errorMsg 错误信息
         */
        void onFailure(String errorMsg);
    }
}