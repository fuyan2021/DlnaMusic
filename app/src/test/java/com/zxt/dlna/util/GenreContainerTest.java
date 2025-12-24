package com.zxt.dlna.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zxt.dlna.util.ApiClient.ApiCallback;
import com.zxt.dlna.util.ApiClient.GenreResponse;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GenreContainer单元测试
 */
public class GenreContainerTest {

    @Mock
    private ApiClient mockApiClient;
    
    @Mock
    private GenreContainer.LoadCallback mockCallback;
    
    private GenreContainer genreContainer;
    private Map<String, String> params;

    public void setUp() {
        // 初始化Mockito注解
        MockitoAnnotations.initMocks(this);
        
        // 创建GenreContainer实例
        genreContainer = new GenreContainer();
        
        // 设置测试参数
        params = new HashMap<>();
        params.put("start", "0");
        params.put("count", "100");
        
        // 替换ApiClient实例为mock对象（适配静态内部类单例模式）
        try {
            Class<?> singletonHolderClass = Class.forName("com.zxt.dlna.util.ApiClient$SingletonHolder");
            java.lang.reflect.Field instanceField = singletonHolderClass.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, mockApiClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testLoadGenreList_Success() {
        // 准备测试数据
        List<GenreResponse.FilterItem> mockFilterItems = new ArrayList<>();
        GenreResponse.FilterItem genreFilterItem = new GenreResponse.FilterItem();
        genreFilterItem.setKey("genres");
        
        List<GenreResponse.GenreData> mockGenreDataList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            GenreResponse.GenreData genreData = new GenreResponse.GenreData();
            genreData.setTypeID(i);
            genreData.setName("Genre " + i);
            genreData.setRealName("Genre Real Name " + i);
            genreData.setGenreImage("image_url_" + i + ".jpg");
            mockGenreDataList.add(genreData);
        }
        genreFilterItem.setData(mockGenreDataList);
        mockFilterItems.add(genreFilterItem);
        
        // 执行测试
        genreContainer.loadGenreList(params, mockCallback);
        
        // 验证ApiClient.getSingleFilterList被调用一次（验证没有分页逻辑）
        verify(mockApiClient, times(1)).getSingleFilterList(eq(params), any(ApiCallback.class));
        
        // 获取ApiCallback并模拟成功响应
        ArgumentCaptor<ApiCallback<List<GenreResponse.FilterItem>>> callbackCaptor =
                ArgumentCaptor.forClass((Class) ApiCallback.class);
        verify(mockApiClient).getSingleFilterList(eq(params), callbackCaptor.capture());
        
        // 模拟成功回调
        callbackCaptor.getValue().onSuccess(mockFilterItems);
        
        // 验证结果
        verify(mockCallback).onSuccess(anyList());
        assertEquals(5, genreContainer.getGenreInfoList().size());
        assertFalse(genreContainer.isLoading());
    }
    
    @Test
    public void testLoadGenreList_Failure() {
        // 执行测试
        genreContainer.loadGenreList(params, mockCallback);
        
        // 获取ApiCallback并模拟失败响应
        ArgumentCaptor<ApiCallback<List<GenreResponse.FilterItem>>> callbackCaptor =
                ArgumentCaptor.forClass((Class) ApiCallback.class);
        verify(mockApiClient).getSingleFilterList(eq(params), callbackCaptor.capture());
        
        // 模拟失败回调
        String errorMessage = "Network error";
        callbackCaptor.getValue().onFailure(errorMessage);
        
        // 验证结果
        verify(mockCallback).onFailure(errorMessage);
        assertTrue(genreContainer.getGenreInfoList().isEmpty());
        assertFalse(genreContainer.isLoading());
    }
    
    @Test
    public void testLoadGenreList_DuplicateCall() {
        // 第一次调用
        genreContainer.loadGenreList(params, mockCallback);
        
        // 第二次调用（重复调用）
        genreContainer.loadGenreList(params, mockCallback);
        
        // 验证第二次调用直接返回失败
        verify(mockCallback).onFailure("正在加载中");
        
        // 验证ApiClient.getSingleFilterList只被调用一次
        verify(mockApiClient, times(1)).getSingleFilterList(anyMap(), any(ApiCallback.class));
    }
    
    @Test
    public void testLoadGenreList_EmptyResponse() {
        // 准备空的测试数据
        List<GenreResponse.FilterItem> mockFilterItems = new ArrayList<>();
        
        // 执行测试
        genreContainer.loadGenreList(params, mockCallback);
        
        // 获取ApiCallback并模拟成功响应（空数据）
        ArgumentCaptor<ApiCallback<List<GenreResponse.FilterItem>>> callbackCaptor =
                ArgumentCaptor.forClass((Class) ApiCallback.class);
        verify(mockApiClient).getSingleFilterList(eq(params), callbackCaptor.capture());
        
        // 模拟成功回调（空数据）
        callbackCaptor.getValue().onSuccess(mockFilterItems);
        
        // 验证结果
        verify(mockCallback).onSuccess(anyList());
        assertTrue(genreContainer.getGenreInfoList().isEmpty());
        assertFalse(genreContainer.isLoading());
    }
    
    @Test
    public void testLoadGenreList_NonGenreData() {
        // 准备非流派数据
        List<GenreResponse.FilterItem> mockFilterItems = new ArrayList<>();
        GenreResponse.FilterItem nonGenreFilterItem = new GenreResponse.FilterItem();
        nonGenreFilterItem.setKey("artists"); // 非流派数据
        
        List<GenreResponse.GenreData> mockGenreDataList = new ArrayList<>();
        GenreResponse.GenreData genreData = new GenreResponse.GenreData();
        genreData.setTypeID(1);
        genreData.setName("Artist 1");
        mockGenreDataList.add(genreData);
        nonGenreFilterItem.setData(mockGenreDataList);
        mockFilterItems.add(nonGenreFilterItem);
        
        // 执行测试
        genreContainer.loadGenreList(params, mockCallback);
        
        // 获取ApiCallback并模拟成功响应
        ArgumentCaptor<ApiCallback<List<GenreResponse.FilterItem>>> callbackCaptor =
                ArgumentCaptor.forClass((Class) ApiCallback.class);
        verify(mockApiClient).getSingleFilterList(eq(params), callbackCaptor.capture());
        
        // 模拟成功回调
        callbackCaptor.getValue().onSuccess(mockFilterItems);
        
        // 验证结果（非流派数据应该被忽略）
        verify(mockCallback).onSuccess(anyList());
        assertTrue(genreContainer.getGenreInfoList().isEmpty());
        assertFalse(genreContainer.isLoading());
    }
}