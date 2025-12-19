package com.zxt.dlna.dms;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.zxt.dlna.R;
import com.zxt.dlna.application.BaseApplication;
import com.zxt.dlna.util.DmsSpUtil;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

import com.zxt.dlna.dms.bean.ComposerInfo;
import com.zxt.dlna.dms.bean.GenreInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuyan
 * 2025/12/18
 * 音乐库DLNA服务 - 后台Service实现
 **/
public class EversoloLibraryService extends Service {
    private AndroidUpnpService upnpService;
    private MediaServer mediaServer;
    private static boolean serverPrepared = false;
    private final static String LOGTAG = "EversoloLibraryService";

    private List<ArtistInfo> artistInfoList = new ArrayList<>();
    private List<AlbumInfo> albumInfoList = new ArrayList<>();
    private List<ComposerInfo> composerInfoList = new ArrayList<>();
    private List<GenreInfo> genreInfoList = new ArrayList<>();
    private List<AudioInfo> audioInfoList = new ArrayList<>();

    // 添加服务绑定状态标志
    private boolean isServiceBound = false;
    // 添加防抖机制标志
    private boolean isProcessing = false;

    // Binder用于客户端与Service通信
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public EversoloLibraryService getService() {
            return EversoloLibraryService.this;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            BaseApplication.upnpService = upnpService;
            isServiceBound = true;
            isProcessing = false;

            Log.v(LOGTAG, "Connected to UPnP Service");

            // 只有在服务器应该运行时才创建媒体服务器
            if (mediaServer == null && DmsSpUtil.getServerOn(getApplicationContext())) {
                try {
                    mediaServer = new MediaServer(getApplicationContext());
                    upnpService.getRegistry().addDevice(mediaServer.getDevice());
                    new Thread(() -> prepareMediaServer()).start();
                } catch (Exception ex) {
                    Log.e(LOGTAG, "Creating demo device failed", ex);
                    // 如果创建失败，确保状态正确重置
                    try {
                        if (isServiceBound) {
                            Context applicationContext = getApplicationContext();
                            if (applicationContext != null) {
                                applicationContext.unbindService(serviceConnection);
                            }
                            isServiceBound = false;
                            upnpService = null;
                            BaseApplication.upnpService = null;
                        }
                    } catch (Exception e) {
                        Log.w(LOGTAG, "Error cleaning up after device creation failure", e);
                    }
                    // 将Toast显示移到主线程
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Context applicationContext = getApplicationContext();
                        if (applicationContext != null) {
                            Toast.makeText(applicationContext, R.string.create_demo_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // 注意：这个方法只在系统杀死服务时调用，而不是在unbindService时
            Log.v(LOGTAG, "Disconnected from UPnP Service by system");
            upnpService = null;
            BaseApplication.upnpService = null;
            isServiceBound = false;
            isProcessing = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // 如果不需要保持服务运行，可以返回false
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止服务器并清理资源
        stopServer();
    }

    private void init() {
        loadArtistInfo();
    }

    public void startServer() {
        // 检查是否正在处理中或服务已绑定
        if (isProcessing || isServiceBound) {
            return;
        }

        isProcessing = true;
        DmsSpUtil.setServerOn(getApplicationContext(), true);

        try {
            Context applicationContext = getApplicationContext();
            if (applicationContext != null) {
                applicationContext.bindService(
                        new Intent(applicationContext, AndroidUpnpServiceImpl.class),
                        serviceConnection, Context.BIND_AUTO_CREATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            isProcessing = false;
        }
    }

    public void stopServer() {
        // 检查是否正在处理中
        if (isProcessing) {
            return;
        }

        isProcessing = true;
        DmsSpUtil.setServerOn(getApplicationContext(), false);

        try {
            // 移除设备注册
            if (upnpService != null && mediaServer != null) {
                try {
                    upnpService.getRegistry().removeDevice(mediaServer.getDevice());
                } catch (Exception e) {
                    // 移除设备失败，记录日志但不影响后续操作
                    Log.e(LOGTAG, "Failed to remove device from registry", e);
                }
            }

            // 关闭媒体服务器，释放资源
            if (mediaServer != null) {
                try {
                    mediaServer.close();
                    Log.v(LOGTAG, "MediaServer resources released");
                } catch (Exception e) {
                    Log.w(LOGTAG, "Error closing MediaServer", e);
                }
            }

            // 取消服务绑定 - 使用专门的异常处理
            try {
                if (isServiceBound) {
                    Context applicationContext = getApplicationContext();
                    if (applicationContext != null) {
                        applicationContext.unbindService(serviceConnection);
                    }
                }
            } catch (IllegalArgumentException e) {
                // 捕获服务未注册的异常，这是正常的边界情况
                Log.w(LOGTAG, "Service not registered when trying to unbind", e);
            } catch (Exception e) {
                // 其他异常
                Log.e(LOGTAG, "Failed to unbind service", e);
            }

            // 清理资源
            mediaServer = null;
            upnpService = null;
            BaseApplication.upnpService = null;

            // 重置ContentTree，确保下次启动时从零开始
            ContentTree.resetContentTree();
            
            // 重置准备标志，确保下次启动时重新初始化
            serverPrepared = false;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 无论成功还是失败，都重置处理状态和绑定状态
            isProcessing = false;
            isServiceBound = false;
        }
    }

    private void prepareMediaServer() {
        // 避免重复初始化
        if (serverPrepared)
            return;
            
        // 获取内容树的根节点
        ContentNode rootNode = ContentTree.getRootNode();

        // 1. 创建或更新音频容器
        Container audioContainer = createOrUpdateAudioContainer(rootNode);
        
        // 2. 加载音频文件
        loadAudioFiles(audioContainer);
        
        // 3. 创建或更新艺术家容器
        createOrUpdateArtistContainer(rootNode);
        
        // 4. 创建或更新专辑容器
        createOrUpdateAlbumContainer(rootNode);
        
        // 5. 创建或更新作曲家容器
        createOrUpdateComposerContainer(rootNode);
        
        // 6. 创建或更新流派容器
        createOrUpdateGenreContainer(rootNode);
        
        // 7. 完成准备工作
        finishPreparation();
    }
    
    /**
     * 创建或更新音频容器
     */
    private Container createOrUpdateAudioContainer(ContentNode rootNode) {
        Container audioContainer;
        // 检查音频容器是否已存在
        if (ContentTree.hasNode(ContentTree.AUDIO_ID)) {
            // 获取现有容器
            audioContainer = ContentTree.getNode(ContentTree.AUDIO_ID).getContainer();
            // 清空现有子项，以便重新加载
            if (audioContainer.getContainers() != null) {
                audioContainer.getContainers().clear();
            }
            if (audioContainer.getItems() != null) {
                audioContainer.getItems().clear();
            }
            audioContainer.setChildCount(0);
        } else {
            // 创建新的音频容器对象
            audioContainer = new Container(
                    ContentTree.AUDIO_ID,         // 容器ID
                    ContentTree.ROOT_ID,          // 父容器ID
                    "Audios",                    // 容器标题
                    "GNaP MediaServer",          // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                            // 子项计数
            );
            audioContainer.setRestricted(true); // 设置为受限容器
            audioContainer.setWriteStatus(WriteStatus.NOT_WRITABLE); // 设置为不可写

            // 将音频容器添加到根节点
            rootNode.getContainer().addContainer(audioContainer);
            // 更新根节点的子容器计数
            rootNode.getContainer().setChildCount(
                    rootNode.getContainer().getChildCount() + 1);
            // 将音频容器添加到内容树
            ContentTree.addNode(ContentTree.AUDIO_ID, new ContentNode(
                    ContentTree.AUDIO_ID, audioContainer));
        }
        return audioContainer;
    }
    
    /**
     * 加载音频文件
     */
    private void loadAudioFiles(Container audioContainer) {
        // 定义需要查询的音频媒体属性列
        String[] audioColumns = {
                MediaStore.Audio.Media._ID,        // 音频ID
                MediaStore.Audio.Media.TITLE,      // 标题
                MediaStore.Audio.Media.DATA,       // 文件路径
                MediaStore.Audio.Media.ARTIST,     // 艺术家
                MediaStore.Audio.Media.MIME_TYPE,  // MIME类型
                MediaStore.Audio.Media.SIZE,       // 文件大小
                MediaStore.Audio.Media.DURATION,   // 时长
                MediaStore.Audio.Media.ALBUM       // 专辑
        };

        // 查询外部存储中的所有音频文件
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                audioColumns, null, null, null);

        // 处理外部存储的音频文件
        processAudioCursor(cursor, audioContainer);

        // 查询SD卡中的音频文件
        String sdcardPath = getSDCardPath(getApplicationContext());
        if (sdcardPath != null) {
            // 只查询SD卡中的音频文件，排除主外部存储
            String selection = MediaStore.Audio.Media.DATA + " LIKE ?";
            String[] selectionArgs = new String[]{sdcardPath + "%"};
            cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    audioColumns, selection, selectionArgs, null);

            // 处理SD卡的音频文件
            processAudioCursor(cursor, audioContainer);
        }
    }
    
    /**
     * 创建或更新艺术家容器
     */
    private void createOrUpdateArtistContainer(ContentNode rootNode) {
        // 首先检查艺术家容器是否已存在，如果不存在则创建
        if (!ContentTree.hasNode(ContentTree.ARTIST_ID)) {
            // 创建艺术家容器对象
            Container artistContainer = new Container(
                    ContentTree.ARTIST_ID,        // 容器ID
                    ContentTree.ROOT_ID,         // 父容器ID
                    "artists",                   // 容器标题
                    "GNaP MediaServer",         // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                           // 子项计数
            );
            artistContainer.setRestricted(true);
            artistContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将艺术家容器添加到根节点
            rootNode.getContainer().addContainer(artistContainer);
            // 更新根节点的子容器计数
            rootNode.getContainer().setChildCount(rootNode.getContainer().getChildCount() + 1);
            // 将艺术家容器添加到内容树
            ContentTree.addNode(ContentTree.ARTIST_ID, new ContentNode(
                    ContentTree.ARTIST_ID, artistContainer
            ));
        }

        // 从ContentTree中获取艺术家容器，确保使用的是同一个对象实例
        Container artistContainer = ContentTree.getNode(ContentTree.ARTIST_ID).getContainer();
        // 为每个艺术家创建子容器
        // 首先清空现有子容器，确保重新创建
        artistContainer.getContainers().clear();
        int artistChildCount = 0;

        for (ArtistInfo artistInfo : artistInfoList) {
            // 使用更简单的ID格式，避免复合ID可能导致的问题
            String artistFoldId = "artist_" + artistInfo.getArtistId();

            // 创建艺术家子容器
            Container artistSubContainer = new Container(
                    artistFoldId,                   // 唯一的容器ID
                    ContentTree.ARTIST_ID,          // 父容器ID
                    artistInfo.getName(),           // 容器标题（艺术家名称）
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );
            artistSubContainer.setRestricted(true);
            artistSubContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 关键：将子容器添加到父容器的子容器列表中
            artistContainer.addContainer(artistSubContainer);
            artistChildCount++;

            // 确保子容器存在于ContentTree中
            if (!ContentTree.hasNode(artistFoldId)) {
                ContentTree.addNode(artistFoldId, new ContentNode(artistFoldId, artistSubContainer));
            } else {
                // 更新ContentTree中的子容器引用
                ContentTree.addNode(artistFoldId, new ContentNode(artistFoldId, artistSubContainer));
            }
            
            // 添加测试曲目到艺术家子容器
            addTestTrackToContainer(artistSubContainer, artistFoldId, artistInfo.getName());
        }

        // 更新艺术家容器的子容器计数
        artistContainer.setChildCount(artistChildCount);
    }
    
    /**
     * 创建或更新专辑容器
     */
    private void createOrUpdateAlbumContainer(ContentNode rootNode) {
        // 首先检查专辑容器是否已存在，如果不存在则创建
        if (!ContentTree.hasNode(ContentTree.ALBUM_ID)) {
            Container albumContainer = new Container(
                    ContentTree.ALBUM_ID,        // 容器ID
                    ContentTree.ROOT_ID,         // 父容器ID
                    "albums",                   // 容器标题
                    "GNaP MediaServer",         // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                           // 子项计数
            );
            albumContainer.setRestricted(true);
            albumContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将专辑容器添加到根节点
            rootNode.getContainer().addContainer(albumContainer);
            // 更新根节点的子容器计数
            rootNode.getContainer().setChildCount(rootNode.getContainer().getChildCount() + 1);
            // 将专辑容器添加到内容树
            ContentTree.addNode(ContentTree.ALBUM_ID, new ContentNode(
                    ContentTree.ALBUM_ID, albumContainer
            ));
        }
        
        // 从ContentTree中获取专辑容器，确保使用的是同一个对象实例
        Container albumContainer = ContentTree.getNode(ContentTree.ALBUM_ID).getContainer();
        // 为每个专辑创建子容器
        // 首先清空现有子容器，确保重新创建
        albumContainer.getContainers().clear();
        int albumChildCount = 0;

        for (AlbumInfo albumInfo : albumInfoList) {
            // 使用更简单的ID格式，避免复合ID可能导致的问题
            String albumFoldId = "album_" + albumInfo.getAlbumId();

            // 创建专辑子容器
            Container albumSubContainer = new Container(
                    albumFoldId,                   // 唯一的容器ID
                    ContentTree.ALBUM_ID,          // 父容器ID
                    albumInfo.getName(),           // 容器标题（专辑名称）
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );
            albumSubContainer.setRestricted(true);
            albumSubContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 关键：将子容器添加到父容器的子容器列表中
            albumContainer.addContainer(albumSubContainer);
            albumChildCount++;

            // 确保子容器存在于ContentTree中
            if (!ContentTree.hasNode(albumFoldId)) {
                ContentTree.addNode(albumFoldId, new ContentNode(albumFoldId, albumSubContainer));
            } else {
                // 更新ContentTree中的子容器引用
                ContentTree.addNode(albumFoldId, new ContentNode(albumFoldId, albumSubContainer));
            }
            
            // 添加测试曲目到专辑子容器
            addTestTrackToContainer(albumSubContainer, albumFoldId, albumInfo.getName());
        }

        // 更新专辑容器的子容器计数
        albumContainer.setChildCount(albumChildCount);
    }
    
    /**
     * 向容器添加测试曲目
     */
    private void addTestTrackToContainer(Container container, String containerId, String artistName) {
        //demo
        String url = "https://er-sycdn.kuwo.cn/027215a86e987b4c41b62e46227b5b1b/6943aab7/resource/30106/trackmedia/M800003CL1jU1Wgcit.mp3";
        Res res = new Res(
                new MimeType("audio", "mpeg"),
                Long.valueOf(4024 * 1024), // 假设大小为1MB
                url
        );

        // 创建测试音乐曲目项
        MusicTrack testTrack = new MusicTrack(
                containerId + "_track1",       // 唯一的项目ID
                containerId,                    // 父容器ID
                "泡沫-邓紫棋", // 标题
                artistName,                     // 创作者
                "泡沫",                      // 专辑
                new PersonWithRole(artistName, "Performer"), // 带角色的表演者
                res                             // 资源对象
        );

        // 添加曲目到容器
        container.addItem(testTrack);
        container.setChildCount(1); // 设置子项计数为1

        // 添加曲目到内容树
        ContentTree.addNode(testTrack.getId(),
                new ContentNode(testTrack.getId(), testTrack, url));
    }



    /**
     * 完成准备工作
     */
    private void finishPreparation() {
        serverPrepared = true; // 设置准备完成标志，防止重复初始化
        loadingComplete();
    }
    
    /**
     * 创建或更新作曲家容器
     */
    private void createOrUpdateComposerContainer(ContentNode rootNode) {
        // 首先检查作曲家容器是否已存在，如果不存在则创建
        if (!ContentTree.hasNode(ContentTree.COMPOSER_ID)) {
            // 创建作曲家容器对象
            Container composerContainer = new Container(
                    ContentTree.COMPOSER_ID,        // 容器ID
                    ContentTree.ROOT_ID,         // 父容器ID
                    "composers",                   // 容器标题
                    "GNaP MediaServer",         // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                           // 子项计数
            );
            composerContainer.setRestricted(true);
            composerContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将作曲家容器添加到根节点
            rootNode.getContainer().addContainer(composerContainer);
            // 更新根节点的子容器计数
            rootNode.getContainer().setChildCount(rootNode.getContainer().getChildCount() + 1);
            // 将作曲家容器添加到内容树
            ContentTree.addNode(ContentTree.COMPOSER_ID, new ContentNode(
                    ContentTree.COMPOSER_ID, composerContainer
            ));
        }

        // 从ContentTree中获取作曲家容器，确保使用的是同一个对象实例
        Container composerContainer = ContentTree.getNode(ContentTree.COMPOSER_ID).getContainer();
        // 为每个作曲家创建子容器
        // 首先清空现有子容器，确保重新创建
        composerContainer.getContainers().clear();
        int composerChildCount = 0;

        for (ComposerInfo composerInfo : composerInfoList) {
            // 使用更简单的ID格式，避免复合ID可能导致的问题
            String composerFoldId = "composer_" + composerInfo.getArtistId();

            // 创建作曲家子容器
            Container composerSubContainer = new Container(
                    composerFoldId,                   // 唯一的容器ID
                    ContentTree.COMPOSER_ID,          // 父容器ID
                    composerInfo.getName(),           // 容器标题（作曲家名称）
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );
            composerSubContainer.setRestricted(true);
            composerSubContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 关键：将子容器添加到父容器的子容器列表中
            composerContainer.addContainer(composerSubContainer);
            composerChildCount++;

            // 确保子容器存在于ContentTree中
            if (!ContentTree.hasNode(composerFoldId)) {
                ContentTree.addNode(composerFoldId, new ContentNode(composerFoldId, composerSubContainer));
            } else {
                // 更新ContentTree中的子容器引用
                ContentTree.addNode(composerFoldId, new ContentNode(composerFoldId, composerSubContainer));
            }
            
            // 添加测试曲目到作曲家子容器
            addTestTrackToContainer(composerSubContainer, composerFoldId, composerInfo.getName());
        }

        // 更新作曲家容器的子容器计数
        composerContainer.setChildCount(composerChildCount);
    }
    
    /**
     * 创建或更新流派容器
     */
    private void createOrUpdateGenreContainer(ContentNode rootNode) {
        // 首先检查流派容器是否已存在，如果不存在则创建
        if (!ContentTree.hasNode(ContentTree.GENRE_ID)) {
            // 创建流派容器对象
            Container genreContainer = new Container(
                    ContentTree.GENRE_ID,        // 容器ID
                    ContentTree.ROOT_ID,         // 父容器ID
                    "genres",                   // 容器标题
                    "GNaP MediaServer",         // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                           // 子项计数
            );
            genreContainer.setRestricted(true);
            genreContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将流派容器添加到根节点
            rootNode.getContainer().addContainer(genreContainer);
            // 更新根节点的子容器计数
            rootNode.getContainer().setChildCount(rootNode.getContainer().getChildCount() + 1);
            // 将流派容器添加到内容树
            ContentTree.addNode(ContentTree.GENRE_ID, new ContentNode(
                    ContentTree.GENRE_ID, genreContainer
            ));
        }

        // 从ContentTree中获取流派容器，确保使用的是同一个对象实例
        Container genreContainer = ContentTree.getNode(ContentTree.GENRE_ID).getContainer();
        // 为每个流派创建子容器
        // 首先清空现有子容器，确保重新创建
        genreContainer.getContainers().clear();
        int genreChildCount = 0;

        for (GenreInfo genreInfo : genreInfoList) {
            // 使用更简单的ID格式，避免复合ID可能导致的问题
            String genreFoldId = "genre_" + genreInfo.getGenreId();

            // 创建流派子容器
            Container genreSubContainer = new Container(
                    genreFoldId,                   // 唯一的容器ID
                    ContentTree.GENRE_ID,          // 父容器ID
                    genreInfo.getName(),           // 容器标题（流派名称）
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );
            genreSubContainer.setRestricted(true);
            genreSubContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 关键：将子容器添加到父容器的子容器列表中
            genreContainer.addContainer(genreSubContainer);
            genreChildCount++;

            // 确保子容器存在于ContentTree中
            if (!ContentTree.hasNode(genreFoldId)) {
                ContentTree.addNode(genreFoldId, new ContentNode(genreFoldId, genreSubContainer));
            } else {
                // 更新ContentTree中的子容器引用
                ContentTree.addNode(genreFoldId, new ContentNode(genreFoldId, genreSubContainer));
            }
            
            // 添加测试曲目到流派子容器
            addTestTrackToContainer(genreSubContainer, genreFoldId, genreInfo.getName());
        }

        // 更新流派容器的子容器计数
        genreContainer.setChildCount(genreChildCount);
    }

    /**
     * 加载完成
     */
    private void loadingComplete() {

    }

    /**
     * 处理音频文件游标，将文件添加到容器和内容树
     */
    private void processAudioCursor(Cursor cursor, Container audioContainer) {
        if (cursor == null) {
            return;
        }

        // 遍历查询结果
        if (cursor.moveToFirst()) {
            do {
                try {
                    // 构建DLNA内容项ID
                    String id = ContentTree.AUDIO_PREFIX
                            + cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                    // 读取音频元数据
                    String title = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String creator = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String filePath = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                    long size = cursor.getLong(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                    long duration = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    String album = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

                    // 创建DLNA资源对象（带异常处理）
                    Res res = null;
                    try {
                        res = new Res(
                                new MimeType(
                                        mimeType.substring(0, mimeType.indexOf('/')),
                                        mimeType.substring(mimeType.indexOf('/') + 1)
                                ),
                                size,
                                "http://" + mediaServer.getAddress() + "/" + id
                        );
                    } catch (Exception e) {
                        Log.w(LOGTAG, "Exception creating Res for file: " + filePath, e);
                        continue; // 跳过当前文件
                    }

                    // 如果资源创建失败，则跳过当前文件
                    if (null == res) {
                        continue;
                    }

                    // 设置音频时长（格式：HH:MM:SS）
                    res.setDuration(
                            duration / (1000 * 60 * 60) + ":"
                                    + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                                    + (duration % (1000 * 60)) / 1000
                    );

                    // 创建DLNA音乐曲目项
                    // 注意：MusicTrack必须设置带角色的艺术家，否则DIDLParser生成时会抛出空指针异常
                    MusicTrack musicTrack = new MusicTrack(
                            id,                     // 项目ID
                            ContentTree.AUDIO_ID,   // 父容器ID
                            title,                  // 标题
                            creator,                // 创作者
                            album,                  // 专辑
                            new PersonWithRole(creator, "Performer"), // 带角色的表演者
                            res                     // 资源对象
                    );

                    // 将音乐曲目添加到音频容器
                    audioContainer.addItem(musicTrack);
                    audioContainer.setChildCount(audioContainer.getChildCount() + 1);
                    // 将音乐曲目添加到内容树
                    ContentTree.addNode(id, new ContentNode(id, musicTrack, filePath));
                } catch (Exception e) {
                    Log.w(LOGTAG, "Error processing audio file", e);
                    // 出现异常时继续处理下一个文件
                    continue;
                }
            } while (cursor.moveToNext());
        }

        // 关闭游标
        try {
            cursor.close();
        } catch (Exception e) {
            Log.w(LOGTAG, "Error closing cursor", e);
        }
    }

    /**
     * 获取SD卡路径
     *
     * @param context 上下文
     * @return SD卡路径，如/mnt/sdcard/，如果没有SD卡则返回null
     */
    private String getSDCardPath(Context context) {
        // 获取所有存储路径
        File[] paths = context.getExternalMediaDirs();

        if (paths != null && paths.length > 0) {
            // 获取主外部存储路径
            String primaryStorage = Environment.getExternalStorageDirectory().getAbsolutePath();

            // 遍历所有存储路径，找到不是主外部存储的路径
            for (File path : paths) {
                if (path != null && !path.getPath().startsWith(primaryStorage)) {
                    // 返回SD卡路径（去掉末尾的/media/Android/data/包名/files/Music部分）
                    int index = path.getPath().indexOf("/Android/data/");
                    if (index != -1) {
                        return path.getPath().substring(0, index);
                    } else {
                        return path.getPath();
                    }
                }
            }
        }

        // 如果没有找到SD卡路径，尝试使用旧方法
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        return null;
    }

    /**
     * 加载艺术家、专辑、作曲家和流派信息
     */
    public void loadArtistInfo() {
        // 创建测试艺术家列表
        List<ArtistInfo> artistInfoList = new ArrayList<>();

        // 添加第一个艺术家
        ArtistInfo artistInfoD = new ArtistInfo();
        artistInfoD.setId(123L);
        artistInfoD.setName("周杰伦");
        artistInfoD.setArtistId(123);
        artistInfoList.add(artistInfoD);

        // 添加第二个艺术家
        ArtistInfo artistInfoE = new ArtistInfo();
        artistInfoE.setId(124L);
        artistInfoE.setName("陈奕迅");
        artistInfoE.setArtistId(124);
        artistInfoList.add(artistInfoE);
        this.artistInfoList = artistInfoList;

        // 创建测试专辑列表
        List<AlbumInfo> albumInfos = new ArrayList<>();
        AlbumInfo albumInfo = new AlbumInfo();
        albumInfo.setId(456L);
        albumInfo.setName("泡沫");
        albumInfo.setAlbumId(456);
        albumInfos.add(albumInfo);
        this.albumInfoList = albumInfos;
        
        // 创建测试作曲家列表
        List<ComposerInfo> composerInfos = new ArrayList<>();
        ComposerInfo composerInfo = new ComposerInfo();
        composerInfo.setId(789L);
        composerInfo.setName("贝多芬");
        composerInfo.setArtistId(789);
        composerInfos.add(composerInfo);
        this.composerInfoList = composerInfos;
        
        // 创建测试流派列表
        List<GenreInfo> genreInfos = new ArrayList<>();
        GenreInfo genreInfo = new GenreInfo();
        genreInfo.setId(101L);
        genreInfo.setName("古典音乐");
        genreInfo.setGenreId(101);
        genreInfos.add(genreInfo);
        this.genreInfoList = genreInfos;
    }

    // 内部数据类定义
    public class ArtistInfo {
        private Long id;
        private String name;
        private int artistId;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getArtistId() {
            return artistId;
        }

        public void setArtistId(int artistId) {
            this.artistId = artistId;
        }
    }

    public class AlbumInfo {
        private Long id;
        private String name;
        private int albumId;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAlbumId() {
            return albumId;
        }

        public void setAlbumId(int albumId) {
            this.albumId = albumId;
        }
    }

    public class AudioInfo {
        private Long id;
        private String name;
        private String path;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}