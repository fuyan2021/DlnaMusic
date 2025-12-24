package com.zxt.dlna.dms;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.zxt.dlna.R;
import com.zxt.dlna.application.BaseApplication;
import com.zxt.dlna.dms.bean.AlbumInfo;
import com.zxt.dlna.dms.bean.ArtistInfo;
import com.zxt.dlna.dms.bean.AudioInfo;
import com.zxt.dlna.dms.bean.ComposerInfo;
import com.zxt.dlna.dms.bean.GenreInfo;
import com.zxt.dlna.util.AlbumContainer;
import com.zxt.dlna.util.ApiClient;
import com.zxt.dlna.util.ArtistContainer;
import com.zxt.dlna.util.ComposerContainer;
import com.zxt.dlna.util.DmsSpUtil;
import com.zxt.dlna.util.FileHelper;
import com.zxt.dlna.util.GenreContainer;
import com.zxt.dlna.util.SingleMusicContainer;
import com.zxt.dlna.util.UpnpUtil;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final static String count = "100";
    private boolean isUpdating = false; // 防止并发更新

    private List<ArtistInfo> artistInfoList = new ArrayList<>();
    private List<ArtistInfo> albumArtistInfoList = new ArrayList<>();
    private List<AlbumInfo> albumInfoList = new ArrayList<>();
    private List<ComposerInfo> composerInfoList = new ArrayList<>();
    private List<GenreInfo> genreInfoList = new ArrayList<>();
    private List<AudioInfo> audioInfoList = new ArrayList<>();

    // 单曲容器，用于管理从getSingleMusics接口获取的音乐列表
    private SingleMusicContainer singleMusicContainer;

    // 艺术家容器，用于管理从getArtists接口获取的艺术家列表
    private ArtistContainer artistContainer;

    // 专辑容器，用于管理从getAlbums接口获取的专辑列表
    private AlbumContainer albumContainer;

    // 专辑艺术家容器，用于管理专辑艺术家列表
    private ArtistContainer albumArtistContainer;

    // 作曲家容器，用于管理从getComposerList接口获取的作曲家列表
    private ComposerContainer composerContainer;

    // 流派容器，用于管理从getSingleFilterList接口获取的流派列表
    private GenreContainer genreContainer;

    // 初始化单曲容器
    private void initSingleMusicContainer() {
        singleMusicContainer = new SingleMusicContainer();
    }

    // 初始化艺术家容器
    private void initArtistContainer() {
        artistContainer = new ArtistContainer();
    }

    // 初始化专辑容器
    private void initAlbumContainer() {
        albumContainer = new AlbumContainer();
    }

    // 初始化专辑艺术家容器
    private void initAlbumArtistContainer() {
        albumArtistContainer = new ArtistContainer();
    }

    // 初始化作曲家容器
    private void initComposerContainer() {
        composerContainer = new ComposerContainer();
    }

    // 初始化流派容器
    private void initGenreContainer() {
        genreContainer = new GenreContainer();
    }

    /**
     * 更新媒体服务器数据
     * 重新请求接口更新所有节点数据
     * 这个更新过程需要时间，数据量越大时间越长
     */
    public void updateMediaServer() {
        // 防止并发更新
        if (isUpdating) {
            Log.w(LOGTAG, "Media server is already updating");
            return;
        }

        isUpdating = true;
        Log.v(LOGTAG, "Starting media server update...");

        // 在UI线程中显示更新开始的提示
//        new Handler(Looper.getMainLooper()).post(() -> {
//            Toast.makeText(getApplicationContext(), "正在更新媒体库...", Toast.LENGTH_SHORT).show();
//        });

        // 重置所有媒体信息列表
        artistInfoList.clear();
        albumArtistInfoList.clear();
        albumInfoList.clear();
        composerInfoList.clear();
        genreInfoList.clear();
        audioInfoList.clear();

        // 重置所有容器对象
        singleMusicContainer = null;
        artistContainer = null;
        albumContainer = null;
        albumArtistContainer = null;
        composerContainer = null;
        genreContainer = null;

        // 重置ContentTree
        ContentTree.resetContentTree();

        // 重置准备标志，确保可以重新初始化
        serverPrepared = false;

        // 重新初始化所有容器
        initSingleMusicContainer();
        initArtistContainer();
        initAlbumContainer();
        initAlbumArtistContainer();
        initComposerContainer();
        initGenreContainer();

        // 获取内容树的根节点
        ContentNode rootNode = ContentTree.getRootNode();

        // 创建或更新音频容器
        Container audioContainer = createOrUpdateAudioContainer(rootNode);

        // 重新加载所有API数据
        int[] pendingCallbacks = {5}; // 五个异步回调：艺术家、专辑艺术家、专辑、作曲家、流派
        final Object lock = new Object();

        // 加载API音乐列表
        loadApiMusicList(audioContainer);

        // 加载API艺术家列表（带回调）
        loadApiArtistList(new ArtistListCallback() {
            @Override
            public void onArtistListLoaded() {
                // 创建或更新艺术家容器
                createOrUpdateArtistContainer(rootNode);
                synchronized (lock) {
                    pendingCallbacks[0]--;
                    if (allCallbacksCompleted(pendingCallbacks)) {
                        finishUpdate();
                    }
                }
            }

            @Override
            public void onArtistListLoadFailed(String errorMsg) {
                Log.e(LOGTAG, "加载艺术家列表失败: " + errorMsg);
                synchronized (lock) {
                    pendingCallbacks[0]--;
                    if (allCallbacksCompleted(pendingCallbacks)) {
                        finishUpdate();
                    }
                }
            }
        });

        // 加载API专辑艺术家列表（带回调）
        loadApiAlbumArtistList(new AlbumArtistListCallback() {
            @Override
            public void onAlbumArtistListLoaded() {
                // 创建或更新专辑艺术家容器
                createOrUpdateAlbumArtistContainer(rootNode);
                synchronized (lock) {
                    pendingCallbacks[1]--;
                    if (allCallbacksCompleted(pendingCallbacks)) {
                        finishUpdate();
                    }
                }
            }

            @Override
            public void onAlbumArtistListLoadFailed(String errorMsg) {
                Log.e(LOGTAG, "加载专辑艺术家列表失败: " + errorMsg);
                synchronized (lock) {
                    pendingCallbacks[1]--;
                    if (allCallbacksCompleted(pendingCallbacks)) {
                        finishUpdate();
                    }
                }
            }
        });

        // 加载API专辑列表（带回调）
        loadApiAlbumList(new AlbumListCallback() {
            @Override
            public void onAlbumListLoaded() {
                // 创建或更新专辑容器
                createOrUpdateAlbumContainer(rootNode);
                synchronized (lock) {
                    pendingCallbacks[2]--;
                    if (allCallbacksCompleted(pendingCallbacks)) {
                        finishUpdate();
                    }
                }
            }

            @Override
            public void onAlbumListLoadFailed(String errorMsg) {
                Log.e(LOGTAG, "加载专辑列表失败: " + errorMsg);
                synchronized (lock) {
                    pendingCallbacks[2]--;
                    if (allCallbacksCompleted(pendingCallbacks)) {
                        finishUpdate();
                    }
                }
            }
        });

        // 加载API作曲家列表（带回调）
        loadApiComposerList(new ComposerListCallback() {
            @Override
            public void onComposerListLoaded() {
                // 创建或更新作曲家容器
                createOrUpdateComposerContainer(rootNode);
                synchronized (lock) {
                    pendingCallbacks[3]--;
                    if (allCallbacksCompleted(pendingCallbacks)) {
                        finishUpdate();
                    }
                }
            }

            @Override
            public void onComposerListLoadFailed(String errorMsg) {
                Log.e(LOGTAG, "加载作曲家列表失败: " + errorMsg);
                synchronized (lock) {
                    pendingCallbacks[3]--;
                    if (allCallbacksCompleted(pendingCallbacks)) {
                        finishUpdate();
                    }
                }
            }
        });

        // 加载API流派列表（带回调）
        loadApiGenreList(new GenreListCallback() {
            @Override
            public void onGenreListLoaded() {
                // 创建或更新流派容器
                createOrUpdateGenreContainer(rootNode);
                synchronized (lock) {
                    pendingCallbacks[4]--;
                    if (allCallbacksCompleted(pendingCallbacks)) {
                        finishUpdate();
                    }
                }
            }

            @Override
            public void onGenreListLoadFailed(String errorMsg) {
                Log.e(LOGTAG, "加载流派列表失败: " + errorMsg);
                synchronized (lock) {
                    pendingCallbacks[4]--;
                    if (allCallbacksCompleted(pendingCallbacks)) {
                        finishUpdate();
                    }
                }
            }
        });
    }

    /**
     * 检查所有回调是否都已完成
     */
    private boolean allCallbacksCompleted(int[] pendingCallbacks) {
        for (int pending : pendingCallbacks) {
            if (pending > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 完成更新操作
     */
    private void finishUpdate() {
        serverPrepared = true;
        isUpdating = false;
        Log.v(LOGTAG, "Media server update completed");

        // 在UI线程中显示更新完成的提示
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(getApplicationContext(), "媒体库更新完成", Toast.LENGTH_SHORT).show();
        });
    }

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

    // 初始化方法
    private void init() {
        initSingleMusicContainer();
        initArtistContainer();
        initAlbumContainer();
        initAlbumArtistContainer();
    }

    /**
     * 服务的启停需要一个缓冲时间，约1-2s，开关时最好loading
     */
    public void startServer() {
        // 检查是否正在处理中或服务已绑定
        if (isProcessing || isServiceBound) {
            return;
        }

        isProcessing = true;
        DmsSpUtil.setServerOn(getApplicationContext(), true);

        try {
            // 获取并设置IP地址
            try {
                String ipAddress = UpnpUtil.getIP();
                if (ipAddress != null && !ipAddress.isEmpty()) {
                    BaseApplication.setHostAddress(ipAddress);
                    Log.d(LOGTAG, "Set host address: " + ipAddress);
                } else {
                    Log.w(LOGTAG, "Failed to get IP address");
                }
            } catch (SocketException e) {
                Log.e(LOGTAG, "Error getting IP address", e);
            }

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

        // 1. 初始化单曲容器
        initSingleMusicContainer();

        // 2. 初始化艺术家容器
        initArtistContainer();

        // 3. 初始化专辑容器
        initAlbumContainer();

        // 4. 初始化专辑艺术家容器
        initAlbumArtistContainer();

        // 5. 初始化作曲家容器
        initComposerContainer();

        // 6. 初始化流派容器
        initGenreContainer();

        // 6. 创建或更新音频容器
        Container audioContainer = createOrUpdateAudioContainer(rootNode);

        // 4. 加载API音乐列表
        loadApiMusicList(audioContainer);

        // 5. 加载API艺术家列表（带回调）
        loadApiArtistList(new ArtistListCallback() {
            @Override
            public void onArtistListLoaded() {
                // 6. 创建或更新艺术家容器
                createOrUpdateArtistContainer(rootNode);
            }

            @Override
            public void onArtistListLoadFailed(String errorMsg) {
                Log.e(LOGTAG, "加载艺术家列表失败: " + errorMsg);
                // 即使加载失败，也完成准备工作，避免服务挂起
                finishPreparation();
            }
        });
        // 6. 加载API专辑艺术家列表（带回调）
        loadApiAlbumArtistList(new AlbumArtistListCallback() {
            @Override
            public void onAlbumArtistListLoaded() {
                // 创建或更新专辑艺术家容器
                createOrUpdateAlbumArtistContainer(rootNode);
            }

            @Override
            public void onAlbumArtistListLoadFailed(String errorMsg) {
                Log.e(LOGTAG, "加载专辑艺术家列表失败: " + errorMsg);
                // 即使加载失败，也完成准备工作，避免服务挂起
                finishPreparation();
            }
        });
        // 7. 加载API专辑列表（带回调）
        loadApiAlbumList(new AlbumListCallback() {
            @Override
            public void onAlbumListLoaded() {
                // 8. 创建或更新专辑容器
                createOrUpdateAlbumContainer(rootNode);
            }

            @Override
            public void onAlbumListLoadFailed(String errorMsg) {
                Log.e(LOGTAG, "加载专辑列表失败: " + errorMsg);
                // 即使加载失败，也继续完成准备工作，避免服务挂起
                finishPreparation();
            }
        });
        // 8. 加载API作曲家列表（带回调）
        loadApiComposerList(new ComposerListCallback() {
            @Override
            public void onComposerListLoaded() {
                // 9. 创建或更新作曲家容器
                createOrUpdateComposerContainer(rootNode);
            }

            @Override
            public void onComposerListLoadFailed(String errorMsg) {
                Log.e(LOGTAG, "加载作曲家列表失败: " + errorMsg);
                // 即使加载失败，也继续完成准备工作，避免服务挂起
                finishPreparation();
            }
        });
        // 10. 加载API流派列表（带回调）
        loadApiGenreList(new GenreListCallback() {
            @Override
            public void onGenreListLoaded() {
                // 11. 创建或更新流派容器
                createOrUpdateGenreContainer(rootNode);
                // 12. 完成准备工作
                finishPreparation();
            }

            @Override
            public void onGenreListLoadFailed(String errorMsg) {
                Log.e(LOGTAG, "加载流派列表失败: " + errorMsg);
                // 即使加载失败，也继续完成准备工作，避免服务挂起
                finishPreparation();
            }
        });
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
                    getApplicationContext().getString(R.string.container_audios),  // 容器标题
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
     * 创建或更新专辑艺术家容器
     */
    private void createOrUpdateAlbumArtistContainer(ContentNode rootNode) {
        // 首先检查专辑艺术家容器是否已存在，如果不存在则创建
        if (!ContentTree.hasNode(ContentTree.ALBUM_ARTIST_ID)) {
            // 创建专辑艺术家容器对象
            Container albumArtistContainer = new Container(
                    ContentTree.ALBUM_ARTIST_ID,        // 容器ID
                    ContentTree.ROOT_ID,         // 父容器ID
                    getString(R.string.container_album_artists),   // 容器标题
                    "GNaP MediaServer",         // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                           // 子项计数
            );
            albumArtistContainer.setRestricted(true);
            albumArtistContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将专辑艺术家容器添加到根节点
            rootNode.getContainer().addContainer(albumArtistContainer);
            // 更新根节点的子容器计数
            rootNode.getContainer().setChildCount(rootNode.getContainer().getChildCount() + 1);
            // 将专辑艺术家容器添加到内容树
            ContentTree.addNode(ContentTree.ALBUM_ARTIST_ID, new ContentNode(
                    ContentTree.ALBUM_ARTIST_ID, albumArtistContainer
            ));
        }

        // 从ContentTree中获取专辑艺术家容器，确保使用的是同一个对象实例
        Container albumArtistContainer = ContentTree.getNode(ContentTree.ALBUM_ARTIST_ID).getContainer();
        // 为每个专辑艺术家创建子容器
        // 首先清空现有子容器，确保重新创建
        albumArtistContainer.getContainers().clear();
        int albumArtistChildCount = 0;

        for (ArtistInfo albumArtistInfo : albumArtistInfoList) {
            // 使用更简单的ID格式，避免复合ID可能导致的问题
            String albumArtistFoldId = "album_artist_" + albumArtistInfo.getId();

            // 创建专辑艺术家子容器
            Container albumArtistSubContainer = new Container(
                    albumArtistFoldId,                   // 唯一的容器ID
                    ContentTree.ALBUM_ARTIST_ID,          // 父容器ID
                    albumArtistInfo.getName(),           // 容器标题（专辑艺术家名称）
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    2                               // 子项计数（专辑）
            );
            albumArtistSubContainer.setRestricted(true);
            albumArtistSubContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 设置专辑艺术家图片URL
            try {
                // 构建专辑艺术家图片URL
                String imageUrl = String.format("http://127.0.0.1:9529/ZidooMusicControl/v2/getImage?id=%d&musicType=0&type=2&target=0x020",
                        albumArtistInfo.getId());
                // 添加albumArtURI属性
                albumArtistSubContainer.addProperty(new org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ALBUM_ART_URI(
                        new java.net.URI(imageUrl)));
            } catch (Exception e) {
                Log.e(LOGTAG, "设置专辑艺术家图片URL失败: " + e.getMessage());
            }

            // 关键：将子容器添加到父容器的子容器列表中
            albumArtistContainer.addContainer(albumArtistSubContainer);
            albumArtistChildCount++;

            // 确保子容器存在于ContentTree中
            if (!ContentTree.hasNode(albumArtistFoldId)) {
                ContentTree.addNode(albumArtistFoldId, new ContentNode(albumArtistFoldId, albumArtistSubContainer));
            } else {
                // 更新ContentTree中的子容器引用
                ContentTree.addNode(albumArtistFoldId, new ContentNode(albumArtistFoldId, albumArtistSubContainer));
            }

            // 为专辑艺术家子容器添加专辑子容器
            String albumArtistAlbumId = albumArtistFoldId + "_albums";
            Container albumArtistAlbumContainer = new Container(
                    albumArtistAlbumId,                  // 唯一的容器ID
                    albumArtistFoldId,                   // 父容器ID
                    "专辑",                         // 容器标题
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );

            albumArtistAlbumContainer.setRestricted(true);
            albumArtistAlbumContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将专辑子容器添加到专辑艺术家子容器
            albumArtistSubContainer.addContainer(albumArtistAlbumContainer);

            // 确保专辑子容器存在于ContentTree中
            if (!ContentTree.hasNode(albumArtistAlbumId)) {
                ContentTree.addNode(albumArtistAlbumId, new ContentNode(albumArtistAlbumId, albumArtistAlbumContainer));
            }

            // 为专辑艺术家子容器添加单曲子容器
            String albumArtistMusicContainerId = albumArtistFoldId + "_musics";
            Container albumArtistMusicContainer = new Container(
                    albumArtistMusicContainerId,         // 唯一的容器ID
                    albumArtistFoldId,                   // 父容器ID
                    "单曲",                         // 容器标题
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );

            albumArtistMusicContainer.setRestricted(true);
            albumArtistMusicContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将单曲子容器添加到专辑艺术家子容器
            albumArtistSubContainer.addContainer(albumArtistMusicContainer);

            // 确保单曲子容器存在于ContentTree中
            if (!ContentTree.hasNode(albumArtistMusicContainerId)) {
                ContentTree.addNode(albumArtistMusicContainerId, new ContentNode(albumArtistMusicContainerId, albumArtistMusicContainer));
            }

            // 使用/getArtistAlbums接口获取该艺术家的专辑列表
            if (ApiClient.getInstance() != null) {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(albumArtistInfo.getId()));
                params.put("start", "0");
                params.put("count", count);
                params.put("artistType", "1");
                ApiClient.getInstance().getArtistAlbums(params, new ApiClient.ApiCallback<ApiClient.AlbumResponse>() {
                    @Override
                    public void onSuccess(ApiClient.AlbumResponse response) {
                        if (response != null && response.getArray() != null) {
                            List<AlbumInfo> artistAlbums = response.getArray();
                            int albumCount = 0;

                            for (AlbumInfo albumInfo : artistAlbums) {
                                // 为专辑创建子容器
                                String albumSubFoldId = albumArtistAlbumId + "_album_" + albumInfo.getId();
                                Container albumSubContainer = new Container(
                                        albumSubFoldId,              // 唯一的容器ID
                                        albumArtistAlbumId,               // 父容器ID
                                        albumInfo.getName(),         // 容器标题（专辑名称）
                                        "GNaP MediaServer",         // 创建者
                                        new DIDLObject.Class("object.container"), // 容器类型
                                        0                           // 子项计数
                                );
                                albumSubContainer.setRestricted(true);
                                albumSubContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

                                // 将专辑子容器添加到艺术家专辑容器
                                albumArtistAlbumContainer.addContainer(albumSubContainer);
                                albumCount++;

                                // 确保专辑子容器存在于ContentTree中
                                if (!ContentTree.hasNode(albumSubFoldId)) {
                                    ContentTree.addNode(albumSubFoldId, new ContentNode(albumSubFoldId, albumSubContainer));
                                } else {
                                    // 更新ContentTree中的子容器引用
                                    ContentTree.addNode(albumSubFoldId, new ContentNode(albumSubFoldId, albumSubContainer));
                                }

                                // 使用albumTypeId参数调用getAlbumMusics接口获取该艺术家的专辑歌曲
                                loadAlbumMusicsForArtist(albumSubContainer, albumSubFoldId, albumInfo, String.valueOf(albumArtistInfo.getId()), 0);
                            }

                            // 更新艺术家专辑容器的子容器计数
                            albumArtistAlbumContainer.setChildCount(albumCount);
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        Log.e(LOGTAG, "获取艺术家专辑列表失败: " + errorMsg);
                    }
                });

                // 使用/getArtistMusics接口获取该艺术家的单曲列表
                Map<String, String> musicParams = new HashMap<>();
                musicParams.put("id", String.valueOf(albumArtistInfo.getId()));
                musicParams.put("start", "0");
                musicParams.put("count", count);
                params.put("artistType", "1");
                params.put("needParse", String.valueOf(true));
                ApiClient.getInstance().getArtistMusics(musicParams, new ApiClient.ApiCallback<ApiClient.MusicResponse>() {
                    @Override
                    public void onSuccess(ApiClient.MusicResponse response) {
                        if (response != null && response.getArray() != null) {
                            List<AudioInfo> artistMusics = response.getArray();

                            // 将歌曲添加到歌曲列表子容器
                            addApiMusicToContainer(artistMusics, albumArtistMusicContainer);

                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        Log.e(LOGTAG, "获取艺术家单曲列表失败: " + errorMsg);
                    }
                });
            } else {
                Log.e(LOGTAG, "apiClient为空，无法获取艺术家专辑和单曲列表");
            }
        }

        // 更新专辑艺术家容器的子容器计数
        albumArtistContainer.setChildCount(albumArtistChildCount);
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
                    getString(R.string.container_artists),   // 容器标题
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
            String artistFoldId = "artist_" + artistInfo.getId();

            // 创建艺术家子容器
            Container artistSubContainer = new Container(
                    artistFoldId,                   // 唯一的容器ID
                    ContentTree.ARTIST_ID,          // 父容器ID
                    artistInfo.getName(),           // 容器标题（艺术家名称）
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    2                               // 子项计数（专辑）
            );
            artistSubContainer.setRestricted(true);
            artistSubContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 设置艺术家图片URL
            try {
                // 构建艺术家图片URL
                String imageUrl = String.format("http://127.0.0.1:9529/ZidooMusicControl/v2/getImage?id=%d&musicType=0&type=2&target=0x020",
                        artistInfo.getId());
                // 添加albumArtURI属性
                artistSubContainer.addProperty(new org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ALBUM_ART_URI(
                        new java.net.URI(imageUrl)));
            } catch (Exception e) {
                Log.e(LOGTAG, "设置艺术家图片URL失败: " + e.getMessage());
            }

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

            // 为艺术家子容器添加专辑子容器
            String artistAlbumId = artistFoldId + "_albums";
            Container artistAlbumContainer = new Container(
                    artistAlbumId,                  // 唯一的容器ID
                    artistFoldId,                   // 父容器ID
                    "专辑",                         // 容器标题
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );

            artistAlbumContainer.setRestricted(true);
            artistAlbumContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将专辑子容器添加到艺术家子容器
            artistSubContainer.addContainer(artistAlbumContainer);

            // 确保专辑子容器存在于ContentTree中
            if (!ContentTree.hasNode(artistAlbumId)) {
                ContentTree.addNode(artistAlbumId, new ContentNode(artistAlbumId, artistAlbumContainer));
            }

            // 为艺术家子容器添加单曲子容器
            String artistMusicContainerId = artistFoldId + "_musics";
            Container artistMusicContainer = new Container(
                    artistMusicContainerId,         // 唯一的容器ID
                    artistFoldId,                   // 父容器ID
                    "单曲",                         // 容器标题
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );

            artistMusicContainer.setRestricted(true);
            artistMusicContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将单曲子容器添加到艺术家子容器
            artistSubContainer.addContainer(artistMusicContainer);

            // 确保单曲子容器存在于ContentTree中
            if (!ContentTree.hasNode(artistMusicContainerId)) {
                ContentTree.addNode(artistMusicContainerId, new ContentNode(artistMusicContainerId, artistMusicContainer));
            }


            // 使用/getArtistAlbums接口获取该艺术家的专辑列表
            if (ApiClient.getInstance() != null) {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(artistInfo.getId()));
                params.put("start", "0");
                params.put("count", count);

                ApiClient.getInstance().getArtistAlbums(params, new ApiClient.ApiCallback<ApiClient.AlbumResponse>() {
                    @Override
                    public void onSuccess(ApiClient.AlbumResponse response) {
                        if (response != null && response.getArray() != null) {
                            List<AlbumInfo> artistAlbums = response.getArray();
                            int albumCount = 0;

                            for (AlbumInfo albumInfo : artistAlbums) {
                                // 为专辑创建子容器
                                String albumSubFoldId = artistAlbumId + "_album_" + albumInfo.getId();
                                Container albumSubContainer = new Container(
                                        albumSubFoldId,              // 唯一的容器ID
                                        artistAlbumId,               // 父容器ID
                                        albumInfo.getName(),         // 容器标题（专辑名称）
                                        "GNaP MediaServer",         // 创建者
                                        new DIDLObject.Class("object.container"), // 容器类型
                                        0                           // 子项计数
                                );
                                albumSubContainer.setRestricted(true);
                                albumSubContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

                                // 将专辑子容器添加到艺术家专辑容器
                                artistAlbumContainer.addContainer(albumSubContainer);
                                albumCount++;

                                // 确保专辑子容器存在于ContentTree中
                                if (!ContentTree.hasNode(albumSubFoldId)) {
                                    ContentTree.addNode(albumSubFoldId, new ContentNode(albumSubFoldId, albumSubContainer));
                                } else {
                                    // 更新ContentTree中的子容器引用
                                    ContentTree.addNode(albumSubFoldId, new ContentNode(albumSubFoldId, albumSubContainer));
                                }

                                // 使用albumTypeId参数调用getAlbumMusics接口获取该艺术家的专辑歌曲
                                loadAlbumMusicsForArtist(albumSubContainer, albumSubFoldId, albumInfo, String.valueOf(artistInfo.getId()), 0);
                            }

                            // 更新艺术家专辑容器的子容器计数
                            artistAlbumContainer.setChildCount(albumCount);
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        Log.e(LOGTAG, "获取艺术家专辑列表失败: " + errorMsg);
                    }
                });

                // 使用/getArtistMusics接口获取该艺术家的单曲列表
                Map<String, String> musicParams = new HashMap<>();
                musicParams.put("id", String.valueOf(artistInfo.getId()));
                musicParams.put("start", "0");
                musicParams.put("count", count);
                params.put("needParse", String.valueOf(true));
                ApiClient.getInstance().getArtistMusics(musicParams, new ApiClient.ApiCallback<ApiClient.MusicResponse>() {
                    @Override
                    public void onSuccess(ApiClient.MusicResponse response) {
                        if (response != null && response.getArray() != null) {
                            List<AudioInfo> artistMusics = response.getArray();

                            // 将歌曲添加到歌曲列表子容器
                            addApiMusicToContainer(artistMusics, artistMusicContainer);

                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        Log.e(LOGTAG, "获取艺术家单曲列表失败: " + errorMsg);
                    }
                });
            } else {
                Log.e(LOGTAG, "apiClient为空，无法获取艺术家专辑和单曲列表");
            }
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
                    getString(R.string.container_albums),   // 容器标题
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
            String albumFoldId = "album_" + albumInfo.getId();

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

            // 设置专辑图片URL
            try {
                // 构建专辑图片URL
                String imageUrl = String.format("http://127.0.0.1:9529/ZidooMusicControl/v2/getImage?id=%d&musicType=0&type=1&target=0x010",
                        albumInfo.getId());
                // 添加albumArtURI属性
                albumSubContainer.addProperty(new org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ALBUM_ART_URI(
                        new java.net.URI(imageUrl)));
            } catch (Exception e) {
                Log.e(LOGTAG, "设置专辑图片URL失败: " + e.getMessage());
            }

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

            // 添加专辑内的歌曲到专辑子容器
            addAlbumSongsToContainer(albumSubContainer, albumFoldId, albumInfo);
        }

        // 更新专辑容器的子容器计数
        albumContainer.setChildCount(albumChildCount);
    }

    /**
     * 向专辑容器添加真实歌曲
     */
    private void addAlbumSongsToContainer(Container container, String containerId, AlbumInfo albumInfo) {
        List<AudioInfo> songs = albumInfo.getSongs();
        if (songs == null || songs.isEmpty()) {
            return;
        }
        Map<String, String> namesMap = new HashMap<>();
        int trackCount = 0;
        for (AudioInfo song : songs) {
            boolean settingOpen = Settings.System.getInt(getApplicationContext().getContentResolver(), "cling_open_iso", 0) == 1;
            if (settingOpen && "iso".equals(song.getExtension())) {
                continue;
            }
            if (song.isDsf() || "dsf".equals(song.getExtension()) || "dff".equals(song.getExtension())) {
                continue;
            }
            // 创建资源对象
            long fileSize = 0;
            // 创建音乐曲目项
            String title = song.getTitle() != null ? song.getTitle() : "未知标题";
            // 检查是否为CUE文件
            if (song.isCue()) {
                // 处理CUE文件
                String filePath = song.getPath() != null ? song.getPath() : "";
                if (!filePath.isEmpty() && namesMap.containsKey(filePath)) {
                    continue;
                }
                title = song.getAlbum();
                namesMap.put(filePath, song.getAlbum());
            }
            try {
                fileSize = FileHelper.getFileSize(song.getPath());
            } catch (Exception e) {
                Log.e(LOGTAG, "获取文件大小失败: " + e.getMessage());
            }
            String path = song.getPath() != null ? song.getPath() : "";
            String filePath = !path.isEmpty() ? path : song.getUrl();
            String uid = containerId + "_track" + (trackCount + 1);
            String httpPath = "http://" + mediaServer.getAddress() + "/" + uid;
            Res res = new Res(
                    new MimeType("audio", "mpeg"),
                    fileSize, // 使用真实的文件大小
                    httpPath // 使用真实的歌曲URI
            );
            if (song.getExtension() != null) {
                title += ("." + song.getExtension());
            }
            MusicTrack track = new MusicTrack(
                    uid,       // 唯一的项目ID
                    containerId,                    // 父容器ID
                    title, // 歌曲标题
                    song.getArtist(),                     // 创作者
                    albumInfo.getName(),                      // 专辑
                    new PersonWithRole(song.getArtist(), "Performer"), // 带角色的表演者
                    res                             // 资源对象
            );

            // 添加曲目到容器
            container.addItem(track);
            trackCount++;

            // 添加曲目到内容树
            ContentTree.addNode(track.getId(),
                    new ContentNode(track.getId(), track, filePath));
        }

        // 更新容器的子项计数
        container.setChildCount(trackCount);
    }

    /**
     * 从API加载音乐列表
     */
    private void loadApiMusicList(final Container audioContainer) {
        Map<String, String> params = new HashMap<>();
        params.put("start", "0");
        params.put("count", count);
        params.put("needParse", String.valueOf(true));
        // 添加日志，显示MusicContainer使用的baseUrl
        ApiClient apiClient = ApiClient.getInstance();
        Log.d(LOGTAG, "SingleMusicContainer将使用的baseUrl: " + apiClient.getBaseUrl());

        singleMusicContainer.loadMusicList(params, new SingleMusicContainer.LoadCallback() {
            @Override
            public void onSuccess(List<AudioInfo> audioInfoList) {
                Log.d(LOGTAG, "API音乐列表加载成功，共 " + audioInfoList.size() + " 首歌曲");

                // 将API获取的音乐添加到音频容器
                addApiMusicToContainer(audioInfoList, audioContainer);
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.d(LOGTAG, "API音乐列表加载失败: " + errorMsg);
            }
        });
    }


    /**
     * 加载API艺术家列表的回调接口
     */
    private interface ArtistListCallback {
        void onArtistListLoaded();

        void onArtistListLoadFailed(String errorMsg);
    }

    /**
     * 加载API专辑艺术家列表的回调接口
     */
    private interface AlbumArtistListCallback {
        void onAlbumArtistListLoaded();

        void onAlbumArtistListLoadFailed(String errorMsg);
    }

    /**
     * 加载API专辑列表的回调接口
     */
    private interface AlbumListCallback {
        void onAlbumListLoaded();

        void onAlbumListLoadFailed(String errorMsg);
    }

    /**
     * 加载API作曲家列表的回调接口
     */
    private interface ComposerListCallback {
        void onComposerListLoaded();

        void onComposerListLoadFailed(String errorMsg);
    }

    /**
     * 加载API流派列表的回调接口
     */
    public interface GenreListCallback {
        void onGenreListLoaded();

        void onGenreListLoadFailed(String errorMsg);
    }

    /**
     * 加载API艺术家列表
     */
    private void loadApiArtistList(final ArtistListCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("start", "0");
        params.put("count", count);
        params.put("artistType", "0");

        // 添加API调试日志
        ApiClient apiClient = ApiClient.getInstance();
        Log.d(LOGTAG, "当前ApiClient baseUrl: " + apiClient.getBaseUrl());
        Log.d(LOGTAG, "加载艺术家列表请求参数: " + params.toString());

        // 使用ArtistContainer加载艺术家列表
        artistContainer.loadArtistList(params, new ArtistContainer.LoadCallback() {
            @Override
            public void onSuccess(List<ArtistInfo> artistInfoList) {
                Log.d(LOGTAG, "API艺术家列表加载成功，共 " + artistInfoList.size() + " 位艺术家");

                // 将API获取的艺术家保存到全局变量
                EversoloLibraryService.this.artistInfoList = artistInfoList;

                // 打印艺术家列表内容，用于调试
                for (ArtistInfo artist : artistInfoList) {
                    Log.d(LOGTAG, "艺术家: " + artist.getId() + " - " + artist.getName());
                }

                // 调用回调通知加载完成
                if (callback != null) {
                    callback.onArtistListLoaded();
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(LOGTAG, "API艺术家列表加载失败: " + errorMsg, new Exception("API调用失败"));

                // 调用回调通知加载失败
                if (callback != null) {
                    callback.onArtistListLoadFailed(errorMsg);
                }
            }
        });
    }

    /**
     * 加载API专辑艺术家列表
     */
    private void loadApiAlbumArtistList(final AlbumArtistListCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("start", "0");
        params.put("count", count);
        params.put("artistType", "1"); // 1表示专辑艺术家

        // 添加API调试日志
        ApiClient apiClient = ApiClient.getInstance();
        Log.d(LOGTAG, "当前ApiClient baseUrl: " + apiClient.getBaseUrl());
        Log.d(LOGTAG, "加载专辑艺术家列表请求参数: " + params.toString());

        // 使用ArtistContainer加载专辑艺术家列表
        albumArtistContainer.loadArtistList(params, new ArtistContainer.LoadCallback() {
            @Override
            public void onSuccess(List<ArtistInfo> albumArtistInfoList) {
                Log.d(LOGTAG, "API专辑艺术家列表加载成功，共 " + albumArtistInfoList.size() + " 位专辑艺术家");

                // 将API获取的专辑艺术家保存到全局变量
                EversoloLibraryService.this.albumArtistInfoList = albumArtistInfoList;

//                // 打印专辑艺术家列表内容，用于调试
//                for (ArtistInfo albumArtist : albumArtistInfoList) {
//                    Log.d(LOGTAG, "专辑艺术家: " + albumArtist.getId() + " - " + albumArtist.getName());
//                }

                // 调用回调通知加载完成
                if (callback != null) {
                    callback.onAlbumArtistListLoaded();
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(LOGTAG, "API专辑艺术家列表加载失败: " + errorMsg, new Exception("API调用失败"));

                // 调用回调通知加载失败
                if (callback != null) {
                    callback.onAlbumArtistListLoadFailed(errorMsg);
                }
            }
        });
    }

    /**
     * 加载API作曲家列表
     */
    private void loadApiComposerList(final ComposerListCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("start", "0");
        params.put("count", count);

        // 添加API调试日志
        ApiClient apiClient = ApiClient.getInstance();
        Log.d(LOGTAG, "当前ApiClient baseUrl: " + apiClient.getBaseUrl());
        Log.d(LOGTAG, "加载作曲家列表请求参数: " + params.toString());

        // 使用ComposerContainer加载作曲家列表
        composerContainer.loadComposerList(params, new ComposerContainer.LoadCallback() {
            @Override
            public void onSuccess(List<ComposerInfo> composerInfoList) {
                Log.d(LOGTAG, "API作曲家列表加载成功，共 " + composerInfoList.size() + " 位作曲家");

                // 将API获取的作曲家保存到全局变量
                EversoloLibraryService.this.composerInfoList = composerInfoList;

                // 打印作曲家列表内容，用于调试
                for (ComposerInfo composer : composerInfoList) {
                    Log.d(LOGTAG, "作曲家: " + composer.getId() + " - " + composer.getName());
                }

                // 调用回调通知加载完成
                if (callback != null) {
                    callback.onComposerListLoaded();
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(LOGTAG, "API作曲家列表加载失败: " + errorMsg, new Exception("API调用失败"));

                // 调用回调通知加载失败
                if (callback != null) {
                    callback.onComposerListLoadFailed(errorMsg);
                }
            }
        });
    }

    /**
     * 加载API流派列表
     */
    private void loadApiGenreList(final GenreListCallback callback) {
        Map<String, String> params = new HashMap<>();
        // 添加分页参数，支持自动分页加载
        params.put("start", "0");
        params.put("count", count);

        // 使用GenreContainer加载流派列表
        genreContainer.loadGenreList(params, new GenreContainer.LoadCallback() {
            @Override
            public void onSuccess(List<GenreInfo> genreInfoList) {
                // 将流派信息保存到全局变量
                EversoloLibraryService.this.genreInfoList = genreInfoList;
                Log.d(LOGTAG, "API流派列表加载成功，共 " + genreInfoList.size() + " 个流派");

                if (callback != null) {
                    callback.onGenreListLoaded();
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(LOGTAG, "API流派列表加载失败: " + errorMsg);
                if (callback != null) {
                    callback.onGenreListLoadFailed(errorMsg);
                }
            }
        });
    }

    /**
     * 为指定艺术家的专辑加载音乐
     *
     * @param container   目标容器
     * @param containerId 容器ID
     * @param albumInfo   专辑信息
     * @param artistId    艺术家ID
     * @param albumType   专辑类型艺术家0,作曲家传1，流派传2
     */
    private void loadAlbumMusicsForArtist(Container container, String containerId, AlbumInfo albumInfo, String artistId, int albumType) {
        // 初始化一个集合来存储所有歌曲
        List<AudioInfo> allSongs = new ArrayList<>();
        // 开始自动分页加载
        loadAlbumMusicsWithPagination(container, containerId, albumInfo, artistId, albumType, 0, 200, allSongs);
    }

    /**
     * 带自动分页的专辑音乐加载
     *
     * @param container   目标容器
     * @param containerId 容器ID
     * @param albumInfo   专辑信息
     * @param artistId    艺术家ID
     * @param albumType   专辑类型艺术家0,作曲家传1，流派传2
     * @param start       起始位置
     * @param count       每页数量
     * @param allSongs    存储所有歌曲的列表
     */
    private void loadAlbumMusicsWithPagination(Container container, String containerId, AlbumInfo albumInfo,
                                               String artistId, int albumType, int start, int count,
                                               List<AudioInfo> allSongs) {
        ApiClient apiClient = ApiClient.getInstance();
        if (apiClient == null) {
            Log.e(LOGTAG, "loadAlbumMusicsWithPagination: apiClient为空，无法加载音乐");
            return;
        }

        try {
            // 准备请求参数
            Map<String, String> params = new HashMap<>();
            params.put("albumTypeId", String.valueOf(artistId));
            params.put("albumType", String.valueOf(albumType));
            params.put("id", String.valueOf(albumInfo.getId()));
            params.put("start", String.valueOf(start));
            params.put("count", String.valueOf(count));
            params.put("needParse", String.valueOf(true));

            // 调用API获取专辑歌曲
            apiClient.getAlbumMusics(params, new ApiClient.ApiCallback<ApiClient.MusicResponse>() {
                @Override
                public void onSuccess(ApiClient.MusicResponse response) {
                    if (response != null && response.getArray() != null) {
                        List<AudioInfo> songList = response.getArray();
                        Log.d(LOGTAG, "loadAlbumMusicsWithPagination: 获取到歌曲数量: " + songList.size() +
                                ", start: " + start + ", count: " + count);

                        // 将当前页歌曲添加到总列表
                        allSongs.addAll(songList);

                        // 检查是否还有更多数据
                        if (response.getStart() + response.getCount() < response.getTotal()) {
                            // 还有更多数据，继续加载下一页
                            loadAlbumMusicsWithPagination(container, containerId, albumInfo, artistId, albumType,
                                    start + count, count, allSongs);
                            return;
                        }

                        // 所有数据加载完成
                        Log.d(LOGTAG, "loadAlbumMusicsWithPagination: 所有歌曲加载完成，共 " + allSongs.size() + " 首");

                        // 直接将所有歌曲添加到专辑容器中
                        addApiMusicToContainer(allSongs, container);

                        // 更新专辑容器的子项计数
                        container.setChildCount(allSongs.size());

                        // 更新ContentTree中的容器信息
                        ContentTree.addNode(containerId, new ContentNode(containerId, container));
                    }
                }

                @Override
                public void onFailure(String errorMsg) {
                    Log.e(LOGTAG, "loadAlbumMusicsWithPagination: 获取专辑歌曲失败: " + errorMsg);
                }
            });
        } catch (Exception e) {
            Log.e(LOGTAG, "loadAlbumMusicsWithPagination: 加载专辑歌曲时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 将API获取的音乐添加到音频容器
     */
    private void addApiMusicToContainer(List<AudioInfo> audioInfoList, Container audioContainer) {
        if (audioInfoList == null || audioInfoList.isEmpty()) {
            return;
        }

        int addedCount = 0;
        Map<String, String> namesMap = new HashMap<>();
        for (AudioInfo audioInfo : audioInfoList) {
            boolean settingOpen = Settings.System.getInt(getApplicationContext().getContentResolver(), "cling_open_iso", 0) == 1;
            if (settingOpen && "iso".equals(audioInfo.getExtension())) {
                continue;
            }
            if (audioInfo.isDsf() || "dsf".equals(audioInfo.getExtension()) || "dff".equals(audioInfo.getExtension())) {
                continue;
            }
            try {
                String title = audioInfo.getTitle() != null ? audioInfo.getTitle().trim() : "未知标题";
                // 检查是否为CUE文件
                if (audioInfo.isCue()) {
                    // 处理CUE文件
                    String filePath = audioInfo.getPath() != null ? audioInfo.getPath() : "";
                    if (!filePath.isEmpty() && namesMap.containsKey(filePath)) {
                        continue;
                    }
                    title = audioInfo.getAlbum();
                    namesMap.put(filePath, audioInfo.getAlbum());
                }
                // 非CUE文件，正常处理

                // 创建MusicTrack对象
                String trackId = "api_track_" + audioContainer.getId() + "_" + audioInfo.getId();
                String artist = audioInfo.getArtist() != null ? audioInfo.getArtist() : "未知艺术家";
                String album = audioInfo.getAlbum() != null ? audioInfo.getAlbum() : "未知专辑";
                String path = audioInfo.getPath() != null ? audioInfo.getPath() : "";
                String filePath = !path.isEmpty() ? path : audioInfo.getUrl();
                String httpPath = "http://" + mediaServer.getAddress() + "/" + trackId;

                // 创建资源对象
                long fileSize = 0;
                try {
                    fileSize = FileHelper.getFileSize(filePath);
                } catch (Exception e) {
                    Log.e(LOGTAG, "获取文件大小失败: " + e.getMessage());
                }
                Res res = new Res(
                        new MimeType("audio", "mpeg"),
                        fileSize, // 使用真实的文件大小
                        httpPath // 使用真实的歌曲URI
                );
                if (audioInfo.getExtension() != null) {
                    title += ("." + audioInfo.getExtension());
                }
                // 创建音乐曲目项
                MusicTrack musicTrack = new MusicTrack(
                        trackId,                      // 唯一的项目ID
                        audioContainer.getId(),       // 父容器ID
                        title,                        // 标题
                        artist,                       // 创作者
                        album,                        // 专辑
                        new PersonWithRole(artist, "Performer"), // 带角色的表演者
                        res                           // 资源对象
                );
                if (fileSize == 0 || title.endsWith(".")) {
                    Log.d(LOGTAG, "添加到单曲容器的歌曲路径: " + title + "!!!" + filePath);
                }
                // 设置图片URL
                try {
                    // 构建图片URL
                    String imageUrl = String.format("http://127.0.0.1:9529/ZidooMusicControl/v2/getImage?id=%d&musicType=%d&type=4&target=0x010",
                            audioInfo.getId(), audioInfo.getType());
                    // 添加albumArtURI属性
                    musicTrack.addProperty(new org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ALBUM_ART_URI(
                            new java.net.URI(imageUrl)));
                } catch (Exception e) {
                    Log.e(LOGTAG, "设置图片URL失败: " + e.getMessage());
                }

                // 添加曲目到容器
                audioContainer.addItem(musicTrack);
                addedCount++;

                // 添加曲目到内容树
                ContentTree.addNode(trackId,
                        new ContentNode(trackId, musicTrack, filePath));

            } catch (Exception e) {
                Log.d(LOGTAG, "添加API音乐到容器失败: " + e.getMessage());
            }
        }

        // 更新容器的子项计数
        audioContainer.setChildCount(audioContainer.getChildCount() + addedCount);
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
                    getString(R.string.container_composers),   // 容器标题
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
            String composerFoldId = "composer_" + composerInfo.getId();

            // 创建作曲家子容器
            Container composerSubContainer = new Container(
                    composerFoldId,                   // 唯一的容器ID
                    ContentTree.COMPOSER_ID,          // 父容器ID
                    composerInfo.getName(),           // 容器标题（作曲家名称）
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    1                               // 子项计数（专辑）
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

            // 为作曲家子容器添加专辑子容器
            String composerAlbumId = composerFoldId + "_albums";
            Container composerAlbumContainer = new Container(
                    composerAlbumId,                  // 唯一的容器ID
                    composerFoldId,                   // 父容器ID
                    "专辑",                         // 容器标题
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );
            composerAlbumContainer.setRestricted(true);
            composerAlbumContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将专辑子容器添加到作曲家子容器
            composerSubContainer.addContainer(composerAlbumContainer);

            // 确保专辑子容器存在于ContentTree中
            if (!ContentTree.hasNode(composerAlbumId)) {
                ContentTree.addNode(composerAlbumId, new ContentNode(composerAlbumId, composerAlbumContainer));
            }

            // 使用/getComposerAlbumList接口获取该作曲家的专辑列表
            if (ApiClient.getInstance() != null) {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(composerInfo.getId()));
                params.put("start", "0");
                params.put("count", count);

                ApiClient.getInstance().getComposerAlbumList(params, new ApiClient.ApiCallback<ApiClient.AlbumResponse>() {
                    @Override
                    public void onSuccess(ApiClient.AlbumResponse response) {
                        if (response != null && response.getArray() != null) {
                            List<AlbumInfo> composerAlbums = response.getArray();
                            int albumCount = 0;

                            for (AlbumInfo albumInfo : composerAlbums) {
                                // 为专辑创建子容器
                                String albumSubFoldId = composerAlbumId + "_album_" + albumInfo.getId();
                                Container albumSubContainer = new Container(
                                        albumSubFoldId,              // 唯一的容器ID
                                        composerAlbumId,             // 父容器ID
                                        albumInfo.getName(),         // 容器标题（专辑名称）
                                        "GNaP MediaServer",         // 创建者
                                        new DIDLObject.Class("object.container"), // 容器类型
                                        0                           // 子项计数
                                );
                                albumSubContainer.setRestricted(true);
                                albumSubContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

                                // 将专辑子容器添加到作曲家专辑容器
                                composerAlbumContainer.addContainer(albumSubContainer);
                                albumCount++;

                                // 确保专辑子容器存在于ContentTree中
                                if (!ContentTree.hasNode(albumSubFoldId)) {
                                    ContentTree.addNode(albumSubFoldId, new ContentNode(albumSubFoldId, albumSubContainer));
                                } else {
                                    // 更新ContentTree中的子容器引用
                                    ContentTree.addNode(albumSubFoldId, new ContentNode(albumSubFoldId, albumSubContainer));
                                }

                                // 使用albumTypeId参数调用getAlbumMusics接口获取该作曲家的专辑歌曲
                                loadAlbumMusicsForArtist(albumSubContainer, albumSubFoldId, albumInfo, String.valueOf(composerInfo.getId()), 1);
                            }

                            // 更新作曲家专辑容器的子容器计数
                            composerAlbumContainer.setChildCount(albumCount);
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        Log.e(LOGTAG, "获取作曲家专辑列表失败: " + errorMsg);
                    }
                });
            } else {
                Log.e(LOGTAG, "apiClient为空，无法获取作曲家专辑列表");
            }

            // 为作曲家子容器添加单曲容器
            String composerAudioId = composerFoldId + "_audios";
            Container composerAudioContainer = new Container(
                    composerAudioId,                  // 唯一的容器ID
                    composerFoldId,                   // 父容器ID
                    "单曲",                         // 容器标题
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );
            composerAudioContainer.setRestricted(true);
            composerAudioContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将单曲子容器添加到作曲家子容器
            composerSubContainer.addContainer(composerAudioContainer);

            // 确保单曲子容器存在于ContentTree中
            if (!ContentTree.hasNode(composerAudioId)) {
                ContentTree.addNode(composerAudioId, new ContentNode(composerAudioId, composerAudioContainer));
            }

            // 使用/getComposerAudioList接口获取该作曲家的单曲列表
            if (ApiClient.getInstance() != null) {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(composerInfo.getId()));
                params.put("start", "0");
                params.put("count", count);

                ApiClient.getInstance().getComposerAudioList(params, new ApiClient.ApiCallback<ApiClient.MusicResponse>() {
                    @Override
                    public void onSuccess(ApiClient.MusicResponse response) {
                        if (response != null && response.getArray() != null) {
                            List<AudioInfo> composerAudios = response.getArray();

                            // 将歌曲添加到单曲容器
                            addApiMusicToContainer(composerAudios, composerAudioContainer);
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        Log.e(LOGTAG, "获取作曲家单曲列表失败: " + errorMsg);
                    }
                });
            } else {
                Log.e(LOGTAG, "apiClient为空，无法获取作曲家单曲列表");
            }


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
                    getString(R.string.container_genres),   // 容器标题
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
                    1                               // 子项计数（专辑）
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

            // 为流派子容器添加专辑子容器
            String genreAlbumId = genreFoldId + "_albums";
            Container genreAlbumContainer = new Container(
                    genreAlbumId,                  // 唯一的容器ID
                    genreFoldId,                   // 父容器ID
                    "专辑",                         // 容器标题
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );
            genreAlbumContainer.setRestricted(true);
            genreAlbumContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将专辑子容器添加到流派子容器
            genreSubContainer.addContainer(genreAlbumContainer);

            // 确保专辑子容器存在于ContentTree中
            if (!ContentTree.hasNode(genreAlbumId)) {
                ContentTree.addNode(genreAlbumId, new ContentNode(genreAlbumId, genreAlbumContainer));
            }

            // 为流派子容器添加单曲子容器
            String genreMusicContainerId = genreFoldId + "_musics";
            Container genreMusicContainer = new Container(
                    genreMusicContainerId,         // 唯一的容器ID
                    genreFoldId,                   // 父容器ID
                    "单曲",                         // 容器标题
                    "GNaP MediaServer",            // 创建者
                    new DIDLObject.Class("object.container"), // 容器类型
                    0                               // 子项计数
            );

            genreMusicContainer.setRestricted(true);
            genreMusicContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

            // 将单曲子容器添加到流派子容器
            genreSubContainer.addContainer(genreMusicContainer);

            // 确保单曲子容器存在于ContentTree中
            if (!ContentTree.hasNode(genreMusicContainerId)) {
                ContentTree.addNode(genreMusicContainerId, new ContentNode(genreMusicContainerId, genreMusicContainer));
            }

            // 使用/getSingleMusics接口获取该流派的单曲列表
            if (ApiClient.getInstance() != null) {
                Map<String, String> params = new HashMap<>();
                // genres参数应该是JSON格式的数组字符串
                params.put("genres", "[" + genreInfo.getGenreId() + "]");
                params.put("needParse", String.valueOf(true));
                params.put("start", "0");
                params.put("count", count);

                ApiClient.getInstance().getSingleMusics(params, new ApiClient.ApiCallback<ApiClient.MusicResponse>() {
                    @Override
                    public void onSuccess(ApiClient.MusicResponse response) {
                        if (response != null && response.getArray() != null) {
                            List<AudioInfo> genreAudios = response.getArray();

                            // 将歌曲添加到单曲子容器
                            addApiMusicToContainer(genreAudios, genreMusicContainer);
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        Log.e(LOGTAG, "获取流派单曲列表失败: " + errorMsg);
                    }
                });
            } else {
                Log.e(LOGTAG, "apiClient为空，无法获取流派单曲列表");
            }

            // 使用/getGenreAlbumList接口获取该流派的专辑列表
            if (ApiClient.getInstance() != null) {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(genreInfo.getGenreId()));
                params.put("start", "0");
                params.put("count", count);

                ApiClient.getInstance().getGenreAlbumList(params, new ApiClient.ApiCallback<ApiClient.AlbumResponse>() {
                    @Override
                    public void onSuccess(ApiClient.AlbumResponse response) {
                        if (response != null && response.getArray() != null) {
                            List<AlbumInfo> genreAlbums = response.getArray();
                            int albumCount = 0;

                            for (AlbumInfo albumInfo : genreAlbums) {
                                // 为专辑创建子容器
                                String albumSubFoldId = genreAlbumId + "_album_" + albumInfo.getId();
                                Container albumSubContainer = new Container(
                                        albumSubFoldId,              // 唯一的容器ID
                                        genreAlbumId,                // 父容器ID
                                        albumInfo.getName(),         // 容器标题（专辑名称）
                                        "GNaP MediaServer",         // 创建者
                                        new DIDLObject.Class("object.container"), // 容器类型
                                        0                           // 子项计数
                                );
                                albumSubContainer.setRestricted(true);
                                albumSubContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);

                                // 将专辑子容器添加到流派专辑容器
                                genreAlbumContainer.addContainer(albumSubContainer);
                                albumCount++;

                                // 确保专辑子容器存在于ContentTree中
                                if (!ContentTree.hasNode(albumSubFoldId)) {
                                    ContentTree.addNode(albumSubFoldId, new ContentNode(albumSubFoldId, albumSubContainer));
                                } else {
                                    // 更新ContentTree中的子容器引用
                                    ContentTree.addNode(albumSubFoldId, new ContentNode(albumSubFoldId, albumSubContainer));
                                }

                                // 为专辑加载歌曲，albumType传2（流派类型），albumTypeId传流派ID
                                loadAlbumMusicsForArtist(albumSubContainer, albumSubFoldId, albumInfo, String.valueOf(genreInfo.getGenreId()), 2);
                            }

                            // 更新流派专辑容器的子容器计数
                            genreAlbumContainer.setChildCount(albumCount);
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        Log.e(LOGTAG, "获取流派专辑列表失败: " + errorMsg);
                    }
                });
            } else {
                Log.e(LOGTAG, "apiClient为空，无法获取流派专辑列表");
            }


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
     * 加载API专辑列表
     */
    private void loadApiAlbumList(final AlbumListCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("start", "0");
        params.put("count", count);

        // 添加API调试日志
        ApiClient apiClient = ApiClient.getInstance();
        Log.d(LOGTAG, "当前ApiClient baseUrl: " + apiClient.getBaseUrl());
        Log.d(LOGTAG, "加载专辑列表请求参数: " + params.toString());

        // 使用AlbumContainer加载专辑列表
        albumContainer.loadAlbumList(params, new AlbumContainer.LoadCallback() {
            @Override
            public void onSuccess(List<AlbumInfo> albumInfoList) {
                Log.d(LOGTAG, "API专辑列表加载成功，共 " + albumInfoList.size() + " 张专辑");

                // 将API获取的专辑保存到全局变量
                EversoloLibraryService.this.albumInfoList = albumInfoList;

                // 打印专辑列表内容，用于调试
                for (AlbumInfo album : albumInfoList) {
                    Log.d(LOGTAG, "专辑: " + album.getId() + " - " + album.getName() + " (" + album.getArtist() + ")");
                }

                // 调用回调通知加载完成
                if (callback != null) {
                    callback.onAlbumListLoaded();
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(LOGTAG, "API专辑列表加载失败: " + errorMsg, new Exception("API调用失败"));

                // 调用回调通知加载失败
                if (callback != null) {
                    callback.onAlbumListLoadFailed(errorMsg);
                }
            }
        });
    }

}