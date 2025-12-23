package com.zxt.dlna.dms;

import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.MusicArtist;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContentDirectoryServiceTest {

    private ContentDirectoryService contentDirectoryService;
    private ContentNode testContainerNode;

    @Before
    public void setUp() throws Exception {
        contentDirectoryService = new ContentDirectoryService();
        
        // 创建测试容器和项目
        Container testContainer = new Container();
        testContainer.setId("test-container");
        testContainer.setTitle("Test Container");
        
        // 添加5个艺术家容器
        List<Container> containers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MusicArtist artist = new MusicArtist();
            artist.setId("artist-" + i);
            artist.setTitle("Artist " + i);
            containers.add(artist);
        }
        
        // 添加50个音乐项目
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            MusicTrack track = new MusicTrack();
            track.setId("track-" + i);
            track.setTitle("Track " + i);
            track.setAlbum("Test Album");
            track.setArtist("Test Artist");
            items.add(track);
        }
        
        testContainer.setContainers(containers);
        testContainer.setItems(items);
        
        // 创建内容节点并添加到内容树
        testContainerNode = new ContentNode(testContainer);
        ContentTree.addNode(testContainerNode);
    }

    @Test
    public void testBrowseNormalRange() throws Exception {
        // 测试正常范围内的分页请求
        BrowseResult result = contentDirectoryService.browse(
                "test-container",
                BrowseFlag.DIRECT_CHILDREN,
                "*",
                10,
                20,
                null
        );
        
        // 验证结果
        assertEquals(55, result.getTotalMatches()); // 5个容器 + 50个项目 = 55个总项
        assertEquals(20, result.getNumberReturned()); // 返回20个结果
        assertTrue(result.getResult().length() > 0); // 结果不为空
    }

    @Test
    public void testBrowseOutOfRangeStartIndex() throws Exception {
        // 测试超出范围的startIndex（日志中报告的情况：totalCount=57, firstResult=57）
        BrowseResult result = contentDirectoryService.browse(
                "test-container",
                BrowseFlag.DIRECT_CHILDREN,
                "*",
                55, // 超出范围，总共有55个项目，索引0-54
                40,
                null
        );
        
        // 验证结果
        assertEquals(55, result.getTotalMatches()); // 总数量仍然正确
        assertEquals(0, result.getNumberReturned()); // 应该返回0个结果
        
        // 验证返回的DIDL内容为空
        DIDLContent didlContent = new DIDLParser().parse(result.getResult());
        assertEquals(0, didlContent.getContainers().size());
        assertEquals(0, didlContent.getItems().size());
    }

    @Test
    public void testBrowseNegativeStartIndex() throws Exception {
        // 测试负数的startIndex
        BrowseResult result = contentDirectoryService.browse(
                "test-container",
                BrowseFlag.DIRECT_CHILDREN,
                "*",
                -10,
                20,
                null
        );
        
        // 验证结果
        assertEquals(55, result.getTotalMatches());
        assertEquals(0, result.getNumberReturned());
    }

    @Test
    public void testBrowseMaxResultsExceedsRemaining() throws Exception {
        // 测试maxResults超过剩余项目数量的情况
        BrowseResult result = contentDirectoryService.browse(
                "test-container",
                BrowseFlag.DIRECT_CHILDREN,
                "*",
                50, // 从第51个项目开始
                20,  // 请求20个，但实际上只剩5个
                null
        );
        
        // 验证结果
        assertEquals(55, result.getTotalMatches());
        assertEquals(5, result.getNumberReturned()); // 应该返回剩余的5个项目
    }

    @Test
    public void testBrowseZeroMaxResults() throws Exception {
        // 测试maxResults为0的情况
        BrowseResult result = contentDirectoryService.browse(
                "test-container",
                BrowseFlag.DIRECT_CHILDREN,
                "*",
                0,
                0,
                null
        );
        
        // 验证结果
        assertEquals(55, result.getTotalMatches());
        assertEquals(55, result.getNumberReturned()); // 应该返回所有项目
    }
}