package com.chasion.controller;


import com.chasion.apis.DiscussPostFeignApi;
import com.chasion.apis.UserFeignApi;
import com.chasion.entity.DiscussPostDTO;
import com.chasion.entity.Page;
import com.chasion.resp.ResultData;
import com.chasion.utils.CommunityConstant;
import com.chasion.utils.HostHolder;
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
//        page.setRows(discussPostFeignApi.discussPostCount(0));
        System.out.println("step in");
        System.out.println(discussPostFeignApi.discussPostCount(0));
        page.setPath("/index?orderMode=" + orderMode);

        // 获取帖子列表
        // 这里是服务器内部请求了，带参数
        ResultData<List<DiscussPostDTO>> listResultData = discussPostFeignApi.discussPosts(0, page, orderMode);
        List<DiscussPostDTO> postDTOS = listResultData.getData();
        List<HashMap<String, Object>> VoList = new ArrayList<>();
        if (postDTOS != null && !postDTOS.isEmpty()) {
            for (DiscussPostDTO postDTO : postDTOS) {
                HashMap<String, Object> Vo = new HashMap<>();
                Vo.put("post", postDTO);
                Vo.put("user", userFeignApi.findUserById(postDTO.getUserId()));
                Vo.put("likeCount", userFeignApi.getLikeCount(1, CommunityConstant.ENTITY_TYPE_POST, postDTO.getId()).getData().get("entityLikeCount"));
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
