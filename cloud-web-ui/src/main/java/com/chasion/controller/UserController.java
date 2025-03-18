package com.chasion.controller;

import com.chasion.apis.UserFeignApi;
import com.chasion.entity.UserDTO;
import com.chasion.utils.CommunityConstant;
import com.chasion.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class UserController {
    /**
     * 个人主页和信息
     * */
    @Autowired
    private UserFeignApi userFeignApi;

    @Autowired
    private HostHolder hostHolder;

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(Model model, @PathVariable int userId) {
        // 需要传入，用户信息：头像、用户名、注册时间、关注了几个人、关注者、获得了多少赞
        UserDTO user = userFeignApi.findUserById(userId);
        model.addAttribute("user", user);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }
        int userLikeCount = userFeignApi.getUserLikeCount(userId);
        model.addAttribute("userLikeCount", userLikeCount);
        // 关注数量
        long followeeCount = userFeignApi.getFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        // 粉丝数量
        long followerCount = userFeignApi.getFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        // 是否关注
        boolean followed = false;
        if (hostHolder.getUser() != null){
            followed = userFeignApi.getIsFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);

        }
        model.addAttribute("followed", followed);


        return "/site/profile";
    }

}
