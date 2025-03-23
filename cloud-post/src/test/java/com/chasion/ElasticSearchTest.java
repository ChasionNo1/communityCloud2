package com.chasion;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.chasion.dao.DiscussPostMapper;
import com.chasion.dao.DiscussPostRepository;
import com.chasion.entity.DiscussPost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

@SpringBootTest
@ContextConfiguration(classes = CloudPostApplication.class)
public class ElasticSearchTest {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private ElasticsearchClient client;



    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testElasticSearchClient() throws IOException {
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(281);
        // 测试添加功能
//        IndexResponse response = client.index(
//                i -> i.index("discusspost")
//                        .id(String.valueOf(discussPost.getId()))
//                        .document(discussPost)
//        );
//        System.out.println(response.result());

        // 测试查询功能
        GetResponse<DiscussPost> discusspost = client.get(g -> g.index("discusspost")
                        .id(String.valueOf(discussPost.getId())),
                DiscussPost.class);
        System.out.println(discusspost);

    }
}
