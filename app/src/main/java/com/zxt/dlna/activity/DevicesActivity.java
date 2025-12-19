package com.zxt.dlna.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zxt.dlna.R;
import com.zxt.dlna.application.BaseApplication;
import com.zxt.dlna.dmp.DeviceItem;
import com.zxt.dlna.dmr.ZxtMediaRenderer;
import com.zxt.dlna.dms.bean.ArtistInfo;
import com.zxt.dlna.dms.ContentNode;
import com.zxt.dlna.dms.ContentTree;
import com.zxt.dlna.dms.MediaServer;
import com.zxt.dlna.util.FixedAndroidHandler;
import com.zxt.dlna.util.Utils;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;
import org.seamless.util.logging.LoggingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DevicesActivity extends Activity {

    private static final Logger log = Logger.getLogger(DevicesActivity.class
            .getName());

    private final static String LOGTAG = "DevicesActivity";

    public static final int DMR_GET_NO = 0;

    public static final int DMR_GET_SUC = 1;

    private static boolean serverPrepared = false;

    private String fileName;

    private ListView mDevLv;

    private ListView mDmrLv;

    private ArrayList<DeviceItem> mDevList = new ArrayList<DeviceItem>();

    private ArrayList<DeviceItem> mDmrList = new ArrayList<DeviceItem>();

    private int mImageContaierId = Integer.valueOf(ContentTree.IMAGE_ID) + 1;

    private long exitTime = 0;

    private DevAdapter mDevAdapter;

    private DevAdapter mDmrDevAdapter;

    private AndroidUpnpService upnpService;

    private DeviceListRegistryListener deviceListRegistryListener;

    private MediaServer mediaServer;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            mDevList.clear();
            mDmrList.clear();

            upnpService = (AndroidUpnpService) service;
            BaseApplication.upnpService = upnpService;

            Log.v(LOGTAG, "Connected to UPnP Service");

            if (mediaServer == null
                    && SettingActivity.getDmsOn(DevicesActivity.this)) {
                try {
                    mediaServer = new MediaServer(DevicesActivity.this);
                    upnpService.getRegistry()
                            .addDevice(mediaServer.getDevice());
                    DeviceItem localDevItem = new DeviceItem(
                            mediaServer.getDevice());

                    deviceListRegistryListener.deviceAdded(localDevItem);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            prepareMediaServer();
                        }
                    }).start();

                } catch (Exception ex) {
                    // TODO: handle exception
                    log.log(Level.SEVERE, "Creating demo device failed", ex);
                    Toast.makeText(DevicesActivity.this,
                                    R.string.create_demo_failed, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
            }

            if (SettingActivity.getRenderOn(DevicesActivity.this)) {
                ZxtMediaRenderer mediaRenderer = new ZxtMediaRenderer(1,
                        DevicesActivity.this);
                upnpService.getRegistry().addDevice(mediaRenderer.getDevice());
                deviceListRegistryListener.dmrAdded(new DeviceItem(
                        mediaRenderer.getDevice()));
            }

            // xgf
            for (Device device : upnpService.getRegistry().getDevices()) {
                if (device.getType().getNamespace().equals("schemas-upnp-org")
                        && device.getType().getType().equals("MediaServer")) {
                    final DeviceItem display = new DeviceItem(device, device
                            .getDetails().getFriendlyName(),
                            device.getDisplayString(), "(REMOTE) "
                            + device.getType().getDisplayString());
                    deviceListRegistryListener.deviceAdded(display);
                }
            }

            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(deviceListRegistryListener);
            // Refresh device list
            upnpService.getControlPoint().search();

            // select first device by default
            if (null != mDevList && mDevList.size() > 0
                    && null == BaseApplication.deviceItem) {
                BaseApplication.deviceItem = mDevList.get(0);
            }
            if (null != mDmrList && mDmrList.size() > 0
                    && null == BaseApplication.dmrDeviceItem) {
                BaseApplication.dmrDeviceItem = mDmrList.get(0);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fix the logging integration between java.util.logging and Android
        // internal logging
        LoggingUtil.resetRootHandler(new FixedAndroidHandler());
        Logger.getLogger("org.teleal.cling").setLevel(Level.INFO);

        setContentView(R.layout.devices);
        init();

        deviceListRegistryListener = new DeviceListRegistryListener();

        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
//		SocketClient socketClient = new SocketClient();
//		socketClient.sendMessage("hello");
    }

    private void init() {

        mDevLv = (ListView) findViewById(R.id.media_server_list);

        if (null != mDevList && mDevList.size() > 0) {
            BaseApplication.deviceItem = mDevList.get(0);
        }

        mDevAdapter = new DevAdapter(DevicesActivity.this, 0, mDevList);
        mDevLv.setAdapter(mDevAdapter);
        mDevLv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                if (null != mDevList && mDevList.size() > 0) {
                    BaseApplication.deviceItem = mDevList.get(arg2);
                    mDevAdapter.notifyDataSetChanged();
                }

            }
        });

        mDmrLv = (ListView) findViewById(R.id.renderer_list);

        if (null != mDmrList && mDmrList.size() > 0) {
            BaseApplication.dmrDeviceItem = mDmrList.get(0);
        }

        mDmrDevAdapter = new DevAdapter(DevicesActivity.this, 0, mDmrList);
        mDmrLv.setAdapter(mDmrDevAdapter);
        mDmrLv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                if (null != mDmrList && mDmrList.size() > 0) {

                    if (null != mDmrList.get(arg2).getDevice()
                            && null != BaseApplication.deviceItem
                            && null != mDmrList.get(arg2).getDevice()
                            .getDetails().getModelDetails()
                            && Utils.DMR_NAME.equals(mDmrList.get(arg2)
                            .getDevice().getDetails().getModelDetails()
                            .getModelName())
                            && Utils.getDevName(
                            mDmrList.get(arg2).getDevice().getDetails()
                                    .getFriendlyName()).equals(
                            Utils.getDevName(BaseApplication.deviceItem
                                    .getDevice().getDetails()
                                    .getFriendlyName()))) {
                        BaseApplication.isLocalDmr = true;
                    } else {
                        BaseApplication.isLocalDmr = false;
                    }
                    BaseApplication.dmrDeviceItem = mDmrList.get(arg2);
                    mDmrDevAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry()
                    .removeListener(deviceListRegistryListener);
        }
        getApplicationContext().unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.search_lan).setIcon(
                android.R.drawable.ic_menu_search);
        menu.add(0, 1, 0, R.string.menu_exit).setIcon(
                android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                searchNetwork();
                break;
            case 1: {
                finish();
                System.exit(0);
                break;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), R.string.exit,
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void searchNetwork() {
        if (upnpService == null)
            return;
        Toast.makeText(this, R.string.searching_lan, Toast.LENGTH_SHORT).show();
        upnpService.getRegistry().removeAllRemoteDevices();
        upnpService.getControlPoint().search();
    }

    public class DeviceListRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry,
                                                 RemoteDevice device) {
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry,
                                                final RemoteDevice device, final Exception ex) {
        }

        /*
         * End of optimization, you can remove the whole block if your Android
         * handset is fast (>= 600 Mhz)
         */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            Log.e("DeviceListRegistryListener",
                    "remoteDeviceAdded:" + device.toString()
                            + device.getType().getType());

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaServer")) {
                final DeviceItem display = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                deviceAdded(display);
            }

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaRenderer")) {
                final DeviceItem dmrDisplay = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                dmrAdded(dmrDisplay);
            }
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            final DeviceItem display = new DeviceItem(device,
                    device.getDisplayString());
            deviceRemoved(display);

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaRenderer")) {
                final DeviceItem dmrDisplay = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                dmrRemoved(dmrDisplay);
            }
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            Log.e("DeviceListRegistryListener",
                    "localDeviceAdded:" + device.toString()
                            + device.getType().getType());

            final DeviceItem display = new DeviceItem(device, device
                    .getDetails().getFriendlyName(), device.getDisplayString(),
                    "(REMOTE) " + device.getType().getDisplayString());
            deviceAdded(display);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            Log.e("DeviceListRegistryListener",
                    "localDeviceRemoved:" + device.toString()
                            + device.getType().getType());

            final DeviceItem display = new DeviceItem(device,
                    device.getDisplayString());
            deviceRemoved(display);
        }

        public void deviceAdded(final DeviceItem di) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (!mDevList.contains(di)) {
                        mDevList.add(di);
                        mDevAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        public void deviceRemoved(final DeviceItem di) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mDevList.remove(di);
                    mDevAdapter.notifyDataSetChanged();
                }
            });
        }

        public void dmrAdded(final DeviceItem di) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (!mDmrList.contains(di)) {
                        mDmrList.add(di);
                        mDmrDevAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        public void dmrRemoved(final DeviceItem di) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mDmrList.remove(di);
                    mDmrDevAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private String[] imageThumbColumns = new String[]{
            MediaStore.Images.Thumbnails.IMAGE_ID,
            MediaStore.Images.Thumbnails.DATA};

    /**
     * prepareMediaServer
     * <p>
     * DLNA媒体服务器内容准备方法，负责初始化媒体内容树结构并从设备媒体库加载媒体文件。
     * 该方法是DMS(数字媒体服务器)功能的核心初始化步骤，主要完成以下工作：
     * 1. 创建内容树的主要容器（视频、音频、图片、艺术家）
     * 2. 从Android媒体库读取视频、音频和图片文件信息
     * 3. 为每个媒体文件创建对应的DLNA内容项
     * 4. 设置媒体文件的元数据（标题、创作者、时长等）
     * 5. 生成媒体文件的HTTP访问URL
     * 6. 将所有内容项添加到内容树中以便被DMP设备发现和访问
     */
    private void prepareMediaServer() {
        // 避免重复初始化
        if (serverPrepared)
            return;
        // 获取内容树的根节点
        ContentNode rootNode = ContentTree.getRootNode();

        // ===== 2. 加载视频媒体文件 =====
        Cursor cursor; // 游标对象，用于查询Android媒体库

        // ===== 3. 创建音频容器 =====
        // 创建音频容器对象，使用简化的构造函数
        Container audioContainer = new Container(
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

        // ===== 4. 加载音频媒体文件 =====
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
        cursor = managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                audioColumns, null, null, null);

        // 遍历查询结果
        if (cursor.moveToFirst()) {
            do {
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
                    Log.w(LOGTAG, "Exception1", e);
                }

                // 如果资源创建失败，则跳过当前文件
                if (null == res) {
                    break;
                }

                // 设置音频时长（格式：HH:MM:SS）
                res.setDuration(
                        duration / (1000 * 60 * 60) + ":" +
                                (duration % (1000 * 60 * 60)) / (1000 * 60) + ":" +
                                (duration % (1000 * 60)) / 1000
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

            } while (cursor.moveToNext());
        }

        // ===== 8. 创建艺术家容器 =====
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
            //demo
            // 为每个艺术家子容器添加测试曲目
            String url = "http://" + mediaServer.getAddress() + "/" + artistFoldId + "_track1";
            Res res = new Res(
                    new MimeType("audio", "mpeg"),
                    Long.valueOf(1024 * 1024), // 假设大小为1MB
                    url
            );

            // 创建测试音乐曲目项
            MusicTrack testTrack = new MusicTrack(
                    artistFoldId + "_track1",       // 唯一的项目ID
                    artistFoldId,                    // 父容器ID
                    "测试曲目 - " + artistInfo.getName(), // 标题
                    artistInfo.getName(),            // 创作者
                    "测试专辑",                      // 专辑
                    new PersonWithRole(artistInfo.getName(), "Performer"), // 带角色的表演者
                    res                             // 资源对象
            );

            // 添加曲目到艺术家子容器
            artistSubContainer.addItem(testTrack);
            artistSubContainer.setChildCount(1); // 设置子项计数为1

            // 添加曲目到内容树
            ContentTree.addNode(testTrack.getId(),
                    new ContentNode(testTrack.getId(), testTrack, url));
        }

        // 更新艺术家容器的子容器计数
        artistContainer.setChildCount(artistChildCount);

        // ===== 9. 标记媒体服务器准备完成 =====
        serverPrepared = true; // 设置准备完成标志，防止重复初始化
    }

    class DevAdapter extends ArrayAdapter<DeviceItem> {

        private static final String TAG = "DeviceAdapter";

        private LayoutInflater mInflater;

        public int dmrPosition = 0;

        private List<DeviceItem> deviceItems;

        public DevAdapter(Context context, int textViewResourceId,
                          List<DeviceItem> objects) {
            super(context, textViewResourceId, objects);
            this.mInflater = ((LayoutInflater) context
                    .getSystemService("layout_inflater"));
            this.deviceItems = objects;
        }

        public int getCount() {
            return this.deviceItems.size();
        }

        public DeviceItem getItem(int paramInt) {
            return this.deviceItems.get(paramInt);
        }

        public long getItemId(int paramInt) {
            return paramInt;
        }

        public View getView(int position, View view, ViewGroup viewGroup) {

            DevHolder holder;
            if (view == null) {
                view = this.mInflater.inflate(R.layout.dmr_item, null);
                holder = new DevHolder();
                holder.filename = ((TextView) view
                        .findViewById(R.id.dmr_name_tv));
                holder.checkBox = ((CheckBox) view.findViewById(R.id.dmr_cb));
                view.setTag(holder);
            } else {
                holder = (DevHolder) view.getTag();
            }

            DeviceItem item = (DeviceItem) this.deviceItems.get(position);
            holder.filename.setText(item.toString());
            if (null != BaseApplication.deviceItem
                    && BaseApplication.deviceItem.equals(item)) {
                holder.checkBox.setChecked(true);
            } else if (null != BaseApplication.dmrDeviceItem
                    && BaseApplication.dmrDeviceItem.equals(item)) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
            return view;
        }

        public final class DevHolder {
            public TextView filename;
            public CheckBox checkBox;
        }

    }
}
