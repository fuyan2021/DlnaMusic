
package com.zxt.dlna.dms;

import java.io.IOException;
import java.net.URI;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import com.zxt.dlna.activity.SettingActivity;
import com.zxt.dlna.application.BaseApplication;
import com.zxt.dlna.util.FileUtil;
import com.zxt.dlna.util.UpnpUtil;
import com.zxt.dlna.util.Utils;

import android.content.Context;
import android.util.Log;

/**
 * MediaServer
 * 
 * DLNA数字媒体服务器(DMS)的核心实现类，负责创建和管理本地DLNA设备实例。
 * 该类实现了UPnP设备的创建、标识和服务绑定，是DLNA媒体共享功能的基础。
 * 
 * 主要功能：
 * 1. 创建符合DLNA规范的UPnP设备实例
 * 2. 绑定ContentDirectory服务到设备
 * 3. 生成唯一设备标识符(UDN)
 * 4. 配置设备元数据(名称、制造商、型号等)
 * 5. 启动HTTP服务器提供媒体内容访问
 */
public class MediaServer {

    private UDN udn; // 设备唯一标识符

    private LocalDevice localDevice; // UPnP本地设备实例

    private final static String deviceType = "MediaServer"; // 设备类型

    private final static int version = 1; // 设备版本

    private final static String LOGTAG = "MediaServer"; // 日志标签

    public final static int PORT = 8192; // HTTP服务器端口
    private Context mContext; // Android上下文
    private HttpServer httpServer; // HTTP服务器引用，用于资源清理

    /**
     * 构造函数，创建并初始化DLNA媒体服务器实例
     * 
     * @param context Android上下文，用于获取资源和系统信息
     * @throws ValidationException 当设备配置无效时抛出
     */
    public MediaServer(Context context ) throws ValidationException {
        mContext = context;
        // 创建UPnP设备类型
        DeviceType type = new UDADeviceType(deviceType, version);

        // 创建设备详情，包含设备名称、制造商信息和型号信息
        DeviceDetails details = new DeviceDetails(
                SettingActivity.getDeviceName(context) + " (" + android.os.Build.MODEL + ")", 
                new ManufacturerDetails(android.os.Build.MANUFACTURER), 
                new ModelDetails(android.os.Build.MODEL, Utils.DMS_DESC, "v1"));

        // 创建并配置ContentDirectory服务
        LocalService service = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);
        service.setManager(new DefaultServiceManager<ContentDirectoryService>(service, ContentDirectoryService.class));

        // 生成设备唯一标识符
        udn = UpnpUtil.uniqueSystemIdentifier("msidms");

        // 创建本地设备实例，包含设备身份、类型、详情、图标和服务
        localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, createDefaultDeviceIcon(), service);

        // 记录设备创建日志
        Log.v(LOGTAG, "MediaServer device created: ");
        Log.v(LOGTAG, "friendly name: " + details.getFriendlyName());
        Log.v(LOGTAG, "manufacturer: " + details.getManufacturerDetails().getManufacturer());
        Log.v(LOGTAG, "model: " + details.getModelDetails().getModelName());

        // 启动HTTP服务器，用于提供媒体内容访问
        try {
            httpServer = new HttpServer(PORT);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
            System.exit(-1);
        }

        Log.v(LOGTAG, "Started Http Server on port " + PORT);
    }

    /**
     * 获取UPnP本地设备实例
     * 
     * @return LocalDevice 配置好的UPnP设备实例，可用于注册到UPnP服务
     */
    public LocalDevice getDevice() {
        return localDevice;
    }

    /**
     * 获取媒体服务器的HTTP访问地址
     * 
     * @return String 服务器地址，格式为"IP:端口"
     */
    public String getAddress() {
        return BaseApplication.getHostAddress() + ":" + PORT;
    }

    /**
     * 创建设备默认图标
     * 
     * @return Icon DLNA设备图标，用于在DLNA控制器上显示
     */
    protected Icon createDefaultDeviceIcon() {
        try {
            // 创建48x48像素、32位色深的PNG图标
            return new Icon("image/png", 48, 48, 32, "msi.png", 
                    mContext.getResources().getAssets().open(FileUtil.LOGO));
        } catch (IOException e) {
            Log.w(LOGTAG, "createDefaultDeviceIcon IOException");
            return null;
        }
    }

    /**
     * 关闭媒体服务器，释放资源
     */
    public void close() {
        // 关闭HTTP服务器
        if (httpServer != null) {
            try {
                httpServer.stop();
                Log.v(LOGTAG, "Http Server stopped");
            } catch (Exception e) {
                Log.w(LOGTAG, "Error stopping Http Server", e);
            }
            httpServer = null;
        }
    }
   
}
