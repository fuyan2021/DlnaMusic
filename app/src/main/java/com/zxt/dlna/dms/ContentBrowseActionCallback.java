/*
 * Copyright (C) 2010 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zxt.dlna.dms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import com.zxt.dlna.activity.ContentActivity;
import com.zxt.dlna.application.ConfigData;
import com.zxt.dlna.dmp.ContentItem;

import android.app.Activity;
import android.os.Handler;

/**
 * ContentBrowseActionCallback
 * <p>
 * DLNA内容浏览回调处理器，负责处理从远程ContentDirectory服务获取内容后的响应。
 * 该类是DMC(数字媒体控制器)浏览DMS(数字媒体服务器)内容的核心组件。
 * <p>
 * 主要功能：
 * 1. 处理ContentDirectory服务的浏览响应
 * 2. 解析DIDL-Lite格式的内容描述
 * 3. 构建本地内容项目列表
 * 4. 按媒体类型(图片、音频、视频)分类内容
 * 5. 通过Handler通知UI层内容获取状态
 *
 * @author Christian Bauer
 * @author DLNA Demo Team
 */
public class ContentBrowseActionCallback extends Browse {

    private static Logger log = Logger.getLogger(ContentBrowseActionCallback.class.getName());

    private Service service; // 远程ContentDirectory服务实例
    private Container container; // 要浏览的容器
    private ArrayList<ContentItem> list; // 存储浏览结果的列表
    private Activity activity; // Android活动上下文
    private Handler handler; // 用于通知UI的Handler

    /**
     * 构造函数
     *
     * @param activity  Android活动上下文，用于在UI线程执行操作
     * @param service   远程ContentDirectory服务实例
     * @param container 要浏览的容器
     * @param list      用于存储浏览结果的列表
     * @param handler   用于向UI发送消息的Handler
     */
    public ContentBrowseActionCallback(Activity activity, Service service,
                                       Container container, ArrayList<ContentItem> list, Handler handler) {
        // 调用父类构造方法，设置浏览参数：浏览直接子项，返回所有属性，无分页，按标题升序排序
        super(service, container.getId(), BrowseFlag.DIRECT_CHILDREN, "*", 0,
                null, new SortCriterion(true, "dc:title"));
        this.activity = activity;
        this.service = service;
        this.container = container;
        this.list = list;
        this.handler = handler;
    }

    /**
     * 当接收到ContentDirectory服务的浏览响应时调用
     *
     * @param actionInvocation 动作调用实例
     * @param didl             解析后的DIDL内容对象
     */
    public void received(final ActionInvocation actionInvocation,
                         final DIDLContent didl) {
        log.fine("Received browse action DIDL descriptor, creating tree nodes");
        // 在UI线程中处理响应结果
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    // 再次检查Activity状态，确保安全执行UI操作
                    if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                        try {
                            // 清空结果列表
                            list.clear();
                            // 首先添加所有子容器
                            for (Container childContainer : didl.getContainers()) {
                                log.fine("add child container " + childContainer.getTitle());
                                list.add(new ContentItem(childContainer, service));
                            }
                            // 然后添加所有子项目
                            for (Item childItem : didl.getItems()) {
                                log.fine("add child item" + childItem.getTitle());
                                list.add(new ContentItem(childItem, service));
                            }
                        } catch (Exception ex) {
                            // 处理异常情况
                            log.fine("Creating DIDL tree nodes failed: " + ex);
                            actionInvocation.setFailure(new ActionException(
                                    ErrorCode.ACTION_FAILED,
                                    "Can't create list childs: " + ex, ex));
                            failure(actionInvocation, null);
                            // 发送内容获取失败消息
                            handler.sendEmptyMessage(ContentActivity.CONTENT_GET_FAIL);
                        }
                        // 清空现有媒体列表
                        ConfigData.listPhotos.clear();

                        // 遍历所有项目，按媒体类型分类
                        Iterator iterator = didl.getItems().iterator();
                        while (iterator.hasNext()) {
                            Item item = (Item) iterator.next();
                            ContentItem contentItem = new ContentItem(item, ContentBrowseActionCallback.this.service);

                            // 检查项目是否有有效标题和资源
                            if ((contentItem.getItem().getTitle().toString() != null)
                                    && (contentItem.getItem().getResources() != null)) {
                                List list = contentItem.getItem().getResources();
                                if ((list.size() != 0)
                                        && (((Res) list.get(0)).getProtocolInfo() != null)
                                        && (((Res) list.get(0)).getProtocolInfo().getContentFormat() != null)) {
                                    // 获取内容格式类型
                                    String formatType = ((Res) list.get(0))
                                            .getProtocolInfo()
                                            .getContentFormat()
                                            .substring(0, ((Res) list.get(0)).getProtocolInfo().getContentFormat().indexOf("/"));

                                    // 根据内容格式类型分类存储
                                    if (formatType.equals("image")) {
                                        // 添加到图片列表
                                        ConfigData.listPhotos.add(new ContentItem(item, ContentBrowseActionCallback.this.service));
                                    } else if (formatType.equals("audio")) {
                                        // 添加到音频列表
                                        ConfigData.listAudios.add(new ContentItem(item, ContentBrowseActionCallback.this.service));
                                    } else {
                                        // 默认为视频
                                        ConfigData.listVideos.add(new ContentItem(item, ContentBrowseActionCallback.this.service));
                                    }
                                }
                            }
                        }

                        // 发送内容获取成功消息
                        handler.sendEmptyMessage(ContentActivity.CONTENT_GET_SUC);
                    }
                }
            });
        }
    }

    @Override
    public void updateStatus(Status status) {

    }


    @Override
    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

    }
}