package com.zxt.dlna.dms;

import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

/**
 * ContentNode
 * 
 * DLNA内容树中的节点类，用于表示媒体内容的层次结构中的一个节点。
 * 该类可以表示两种类型的节点：容器(Container)和项目(Item)。
 * 容器类似于文件夹，可以包含其他容器或项目；项目表示具体的媒体文件。
 * 
 * 主要功能：
 * 1. 封装容器或项目的信息
 * 2. 提供节点类型判断功能
 * 3. 存储节点的唯一标识符
 * 4. 对于项目类型，存储文件的完整路径
 */
public class ContentNode {
		private Container container; // 容器对象，当节点为容器类型时使用
		private Item item; // 项目对象，当节点为项目类型时使用
		private String id; // 节点的唯一标识符
		private String fullPath; // 对于项目类型，存储文件的完整路径
		private boolean isItem; // 标识节点是否为项目类型
		
		/**
		 * 创建容器类型的内容节点
		 * 
		 * @param id 节点的唯一标识符
		 * @param container 容器对象
		 */
		public ContentNode(String id, Container container) {
			this.id = id;
			this.container = container;
			this.fullPath = null; // 容器没有文件路径
			this.isItem = false; // 标识为非项目类型
		}
		
		/**
		 * 创建项目类型的内容节点
		 * 
		 * @param id 节点的唯一标识符
		 * @param item 项目对象
		 * @param fullPath 文件的完整路径
		 */
		public ContentNode(String id, Item item, String fullPath) {
			this.id = id;
			this.item = item;
			this.fullPath = fullPath; // 存储文件路径
			this.isItem = true; // 标识为项目类型
		}
		
		/**
		 * 获取节点的唯一标识符
		 * 
		 * @return String 节点ID
		 */
		public String getId() {
			return id;
		}
		
		/**
		 * 获取容器对象
		 * 
		 * @return Container 容器对象，如果节点不是容器类型则可能为null
		 */
		public Container getContainer() {
			return container;
		}
		
		/**
		 * 获取项目对象
		 * 
		 * @return Item 项目对象，如果节点不是项目类型则可能为null
		 */
		public Item getItem() {
			return item;
		}
		
		/**
		 * 获取文件的完整路径
		 * 
		 * @return String 仅当节点为项目类型且路径存在时返回完整路径，否则返回null
		 */
		public String getFullPath() {
			if (isItem && fullPath != null) {
				return fullPath;
			}
			return null;
		}
		
		/**
		 * 判断节点是否为项目类型
		 * 
		 * @return boolean 如果是项目类型返回true，否则返回false
		 */
		public boolean isItem() {
			return isItem;
		}
}
