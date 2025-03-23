package com.chasion.controller;

import com.chasion.apis.DiscussPostFeignApi;
import com.chasion.apis.UserFeignApi;
import com.chasion.entity.DiscussPostDTO;
import com.chasion.entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chasion.utils.CommunityConstant.ENTITY_TYPE_POST;

@Controller
public class SearchController {

    @Autowired
    private DiscussPostFeignApi discussPostFeignApi;

    @Autowired
    private UserFeignApi userFeignApi;


    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(Model model, String keyword, Page page) throws IOException {

        // 搜索帖子
        List<DiscussPostDTO> discussPosts = discussPostFeignApi.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit()).getData();
        // 聚合数据
        List<Map<String, Object>> postVO = new ArrayList<>();
        if (discussPosts != null){
            for (DiscussPostDTO post : discussPosts) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userFeignApi.findUserById(post.getUserId()));
                // 点赞数量
                // entityLikeCount
                String entityLikeCount = userFeignApi.getLikeCount(null, ENTITY_TYPE_POST, post.getId()).getData().get("entityLikeCount");
                map.put("likeCount", entityLikeCount);
                postVO.add(map);
            }
        }
        model.addAttribute("discussPosts", postVO);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(discussPosts == null ? 0 : discussPosts.size());
        page.setLimit(5);

        return "/site/search";
    }
}
