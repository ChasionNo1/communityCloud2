package com.chasion.controller;

//import com.chasion.entity.Event;
import com.chasion.annotation.LoginRequired;
import com.chasion.apis.UserFeignApi;
import com.chasion.entity.FollowListDTO;
import com.chasion.entity.Page;
import com.chasion.entity.UserDTO;
import com.chasion.utils.CommunityConstant;
import com.chasion.utils.CommunityUtil;
import com.chasion.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class FollowController implements CommunityConstant{

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserFeignApi userFeignApi;

//    @Autowired
//    private EventProducer eventProducer;


    // 关注功能，异步请求
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String follow(int entityType, int entityId) {
        UserDTO user = hostHolder.getUser();
        // 关注
        // 这个服务放在哪里？也在user服务里吧
        userFeignApi.follow(user.getId(), entityType, entityId);
        // 触发关注事件
//        Event event = new Event()
//                .setTopic(TOPIC_FOLLOW)
//                .setUserId(hostHolder.getUser().getId())
//                .setEntityType(entityType)
//                .setEntityId(entityId)
//                .setEntityUserId(entityId);
//        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "已关注");
    }

    // 取消关注，也是异步请求
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        UserDTO user = hostHolder.getUser();
        userFeignApi.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注");
    }

//     获取关注列表
    @RequestMapping(path = "/followee/list/{userId}", method = RequestMethod.GET)
    public String followList(@PathVariable int userId, Model model, Page page) {
        // 设置分页信息
        page.setRows((int) userFeignApi.getFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));
        page.setPath("/follow/list/" + userId);
        page.setLimit(10);
        // 设置volist
        List<FollowListDTO> followeeList = userFeignApi.getFolloweeList(userId, CommunityConstant.ENTITY_TYPE_USER, page.getOffset(), page.getLimit()).getData();
        model.addAttribute("followeeList", followeeList);
        UserDTO user = userFeignApi.findUserById(userId);
        model.addAttribute("user", user);
        //
        return "/site/followee";

    }


    @RequestMapping(path = "/follower/list/{userId}", method = RequestMethod.GET)
    public String followerList(@PathVariable int userId, Model model, Page page) {
        // 设置分页信息
        page.setRows((int) userFeignApi.getFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId));
        page.setPath("/follower/list/" + userId);
        page.setLimit(10);
        // 设置volist
        List<FollowListDTO> followerList = userFeignApi.getFollowerList(CommunityConstant.ENTITY_TYPE_USER, userId, page.getOffset(), page.getLimit()).getData();
        model.addAttribute("followerList", followerList);
        UserDTO user = userFeignApi.findUserById(userId);
        model.addAttribute("user", user);
        return "/site/follower";
    }

}


