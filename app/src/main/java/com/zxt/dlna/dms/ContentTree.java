package com.zxt.dlna.dms;

import java.util.HashMap;

import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;


/**
 * ContentTree
 * 
 * DLNA媒体内容的层次化管理类，负责维护媒体内容的树形结构和提供节点访问接口。
 * 该类实现了媒体内容的组织和检索功能，是ContentDirectoryService的数据支撑。
 * 
 * 主要功能：
 * 1. 维护媒体内容的树形结构(容器和项目)
 * 2. 提供节点的添加、获取和查询功能
 * 3. 定义预定义节点ID常量
 * 4. 管理根容器的创建和初始化
 */
public class ContentTree {

	public final static String ROOT_ID = "0"; // 根容器ID
	public final static String VIDEO_ID = "1"; // 视频容器ID
	public final static String AUDIO_ID = "2"; // 音频容器ID
	public final static String IMAGE_ID = "3"; // 图片容器ID
	public final static String IMAGE_FOLD_ID = "4"; // 图片文件夹ID
	/**新增功能*/
	public final static String ARTIST_ID = "5"; // 艺术家容器ID
	public final static String ARTIST_FOLD_ID = "6"; // 艺术家文件容器ID
	public final static String ALBUM_ID = "7"; // 专辑容器ID
	public final static String COMPOSER_ID = "8"; // 作曲家容器ID
	public final static String GENRE_ID = "9"; // 流派容器ID
	public final static String VIDEO_PREFIX = "video-item-"; // 视频项目ID前缀
	public final static String AUDIO_PREFIX = "audio-item-"; // 音频项目ID前缀
	public final static String IMAGE_PREFIX = "image-item-"; // 图片项目ID前缀
	
	private static HashMap<String, ContentNode> contentMap = new HashMap<String, ContentNode>(); // 节点映射表

	private static ContentNode rootNode = createRootNode(); // 根节点

	/**
	 * 构造函数
	 * 
	 * 注：由于ContentTree主要使用静态方法和静态变量，实际使用中通常不需要创建实例
	 */
	public ContentTree() {};

	/**
	 * 创建内容树的根节点
	 * 
	 * @return ContentNode 配置好的根节点
	 */
	protected static ContentNode createRootNode() {
		// 创建根容器
		Container root = new Container();
		root.setId(ROOT_ID); // 设置唯一标识符
		root.setParentID("-1"); // 根节点没有父节点
		root.setTitle("GNaP MediaServer root directory"); // 设置标题
		root.setCreator("GNaP Media Server"); // 设置创建者
		root.setRestricted(true); // 内容是否受限制
		root.setSearchable(true); // 是否支持搜索
		root.setWriteStatus(WriteStatus.NOT_WRITABLE); // 写入状态
		root.setChildCount(0); // 初始子节点数量
		
		// 创建内容节点并添加到映射表
		ContentNode rootNode = new ContentNode(ROOT_ID, root);
		contentMap.put(ROOT_ID, rootNode);
		return rootNode;
	}
	
	/**
	 * 获取内容树的根节点
	 * 
	 * @return ContentNode 根节点
	 */
	public static ContentNode getRootNode() {
		return rootNode;
	}
	
	/**
	 * 根据ID获取内容节点
	 * 
	 * @param id 节点的唯一标识符
	 * @return ContentNode 对应的内容节点，如果不存在则返回null
	 */
	public static ContentNode getNode(String id) {
		if( contentMap.containsKey(id)) {
			return contentMap.get(id);
		}
		return null;
	}
	
	/**
	 * 检查是否存在指定ID的节点
	 * 
	 * @param id 要检查的节点ID
	 * @return boolean 如果节点存在则返回true
	 */
	public static boolean hasNode(String id) {
		return contentMap.containsKey(id);
	}

	/**
	 * 添加内容节点到内容树
	 * 
	 * @param ID 节点的唯一标识符
	 * @param Node 要添加的内容节点
	 */
	public static void addNode(String ID, ContentNode Node) {
		contentMap.put(ID, Node);
	}
	
	/**
	 * 重置内容树
	 * 清空所有节点并重新创建根节点
	 */
	public static void resetContentTree() {
		// 清空所有节点
		contentMap.clear();
		// 重新创建根节点
		rootNode = createRootNode();
	}
}
