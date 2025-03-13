package com.chasion.controller;

import com.chasion.apis.DiscussPostFeignApi;
import com.chasion.entity.UserDTO;
import com.chasion.utils.CommunityUtil;
import com.chasion.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PostController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostFeignApi discussPostFeignApi;

    /**
     * 帖子模块
     * */
    // 处理发布帖子的异步请求
    // 从页面接收数据，做一下过滤，然后添加到数据库中，然后刷新一下页面
    @PostMapping("/discuss/add")
    @ResponseBody
    public String addDiscussPost(@RequestParam String title, @RequestParam String content) {
        // 这里调用post服务，封装post不好在这里进行，这里只有postDTO
        UserDTO user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登录");
        }
        // 这里是登录了，通过openfeign调用
        discussPostFeignApi.addDiscussPost(user.getId(), title, content);

        return CommunityUtil.getJSONString(0, "发布成功");
    }

}
