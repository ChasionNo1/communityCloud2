package com.chasion.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import com.chasion.dao.DiscussPostRepository;
import com.chasion.entity.DiscussPost;
import com.chasion.entity.DiscussPostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchClient client;

    public void saveDiscussPost(DiscussPostDTO discussPost) {
        discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public List<DiscussPostDTO> searchDiscussPost(String keyword, int current, int limit) throws IOException {
        // 8.5 版本的实现
        // 创建高亮构建器，指定需要高亮的字段和显示样式
        Highlight highlight = Highlight.of(h -> h
                .fields("title", f -> f)    // 标题高亮配置
                .fields("content", f -> f) // 内容高亮配置
                .preTags("<em class=\"highlight\">")  // 更友好的高亮样式
                .postTags("</em>")
        );
        // ==================== 构建搜索请求 ====================
        SearchResponse<DiscussPostDTO> response = client.search(s -> s
                        .index("discusspost")
                        .query(q -> q
                                .multiMatch(m -> m      // 多字段匹配查询
                                        .fields("title", "content") // 同时搜索标题和内容
                                        .query(keyword)
                                )
                        )
                        .highlight(highlight)
                        .from(current) // 分页起始位置
                        .size(limit),             // 每页数量
                DiscussPostDTO.class
        );

        // ==================== 处理高亮结果 ====================
        return response.hits().hits().stream()
                .map(hit -> {
                    DiscussPostDTO discussPost = hit.source();

                    // 处理标题高亮
                    List<String> titleHighlights = hit.highlight().get("title");
                    if (titleHighlights != null && !titleHighlights.isEmpty()) {
                        discussPost.setTitle(titleHighlights.get(0)); // 取第一个高亮片段
                    }

                    // 处理内容高亮
                    List<String> contentHighlights = hit.highlight().get("content");
                    if (contentHighlights != null && !contentHighlights.isEmpty()) {
                        // 取前200字符的高亮内容（根据需求调整）
                        String shortHighlight = contentHighlights.get(0);
                        if (shortHighlight.length() > 200) {
                            shortHighlight = shortHighlight.substring(0, 200) + "...";
                        }
                        discussPost.setContent(shortHighlight);
                    }

                    return discussPost;
                })
                .collect(Collectors.toList());
    }


}
