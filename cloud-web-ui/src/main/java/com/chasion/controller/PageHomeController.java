package com.chasion.cloudwebui.controller;


import com.chasion.cloudcommonsapi.apis.DiscussPostFeignApi;
import com.chasion.cloudcommonsapi.apis.UserFeignApi;
import com.chasion.cloudcommonsapi.entity.DiscussPostDTO;
import com.chasion.cloudcommonsapi.entity.Page;
import com.chasion.cloudcommonsapi.resp.ResultData;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class PageHomeController {

    @Resource
    private DiscussPostFeignApi discussPostFeignApi;

    @Resource
    private UserFeignApi userFeignApi;

    @GetMapping("/index")
    public String index(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        // 在这个controller里要完成对首页数据的渲染
        // 需要帖子列表+帖子总数+user+likecount等数据，需要来自不同的模块
        page.setRows(discussPostFeignApi.discussPostCount());
        page.setPath("/index?orderMode=" + orderMode);

        // 获取帖子列表
        // 这里是服务器内部请求了，带参数
        ResultData<List<DiscussPostDTO>> listResultData = discussPostFeignApi.discussPosts(page, orderMode);
        List<DiscussPostDTO> postDTOS = listResultData.getData();
        List<HashMap<String, Object>> VoList = new ArrayList<>();
        if (postDTOS != null && !postDTOS.isEmpty()) {
            for (DiscussPostDTO postDTO : postDTOS) {
                HashMap<String, Object> Vo = new HashMap<>();
                Vo.put("post", postDTO);
                Vo.put("user", userFeignApi.findUserById(postDTO.getUserId()));
                Vo.put("likeCount", 1);
                VoList.add(Vo);

            }
        }
        model.addAttribute("VoList", VoList);
        model.addAttribute("orderMode", orderMode);

        return "index";
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "test";
    }

}
