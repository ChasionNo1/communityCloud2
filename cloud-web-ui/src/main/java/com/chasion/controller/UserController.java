package com.chasion.controller;

import com.chasion.apis.CommentFeignApi;
import com.chasion.apis.DiscussPostFeignApi;
import com.chasion.apis.UserFeignApi;
import com.chasion.entity.CommentDTO;
import com.chasion.entity.DiscussPostDTO;
import com.chasion.entity.Page;
import com.chasion.entity.UserDTO;
import com.chasion.utils.CommunityConstant;
import com.chasion.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/user")
@RefreshScope
public class UserController {
    /**
     * 个人主页和信息
     * */
    @Autowired
    private UserFeignApi userFeignApi;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostFeignApi discussPostFeignApi;

    @Autowired
    private CommentFeignApi commentFeignApi;

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

    /**
     * 获取个人发布的帖子列表，支持分页
     * 需要的信息有：
     * 1、发布的帖子总数
     * 2、每条帖子的标题和内容，获得的赞和发布时间
     *
     * */
    @RequestMapping(path = "/discussPost/{userId}", method = RequestMethod.GET)
    public String getMyPostList(Model model, @PathVariable("userId") int userId, Page page) {
        UserDTO user = userFeignApi.findUserById(userId);
        model.addAttribute("user", user);
        // 设置分页信息
        int discussPostRows = discussPostFeignApi.discussPostCount(userId);
        model.addAttribute("discussPostRows", discussPostRows);
        page.setRows(discussPostRows);
        page.setLimit(10);
        page.setPath("/user/discussPost" + userId);
        // 查找某人发布过的帖子
        List<DiscussPostDTO> postList = discussPostFeignApi.discussPosts(userId, page, 0).getData();
        // 封装volist
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        if (!postList.isEmpty()){
            for (DiscussPostDTO post : postList){
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", post.getId());
                map.put("title", post.getTitle());
                map.put("content", post.getContent());
                map.put("createTime", post.getCreateTime());
                Object likeCount = userFeignApi.getLikeCount(0, CommunityConstant.ENTITY_TYPE_POST, post.getId()).getData().get("entityLikeCount");
                map.put("likeCount", likeCount);
                list.add(map);
            }
        }
        model.addAttribute("postList", list);

        return "/site/my-post";
    }

    /**
     * 回复列表：支持分页
     * 需要的信息有：
     * 回复的帖子总数
     * 回复帖子的id，回复帖子的标题，回复的内容，回复的时间
     *
     *
     * */
    @RequestMapping(path = "/reply/{userId}", method = RequestMethod.GET)
    public String getMyReplyList(Model model, @PathVariable("userId") int userId, Page page) {
        UserDTO user = userFeignApi.findUserById(userId);
        model.addAttribute("user", user);
        page.setPath("/user/reply" + userId);
        page.setLimit(10);
        // 对帖子的评论
        int commentCount = commentFeignApi.getCommentCount(CommunityConstant.ENTITY_TYPE_POST, userId);
        model.addAttribute("commentCount", commentCount);
        page.setRows(commentCount);
        List<CommentDTO> comments = commentFeignApi.getCommentList(CommunityConstant.ENTITY_TYPE_POST, userId, page.getOffset(), page.getLimit()).getData();
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        if (comments != null && !comments.isEmpty()){
            for (CommentDTO comment : comments){
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", comment.getEntityId());
                map.put("content", comment.getContent());
                map.put("createTime", comment.getCreateTime());
                DiscussPostDTO post = discussPostFeignApi.getDiscussPost(comment.getEntityId());
                map.put("title", post.getTitle());
                list.add(map);
            }
        }
        model.addAttribute("commentList", list);
        return "/site/my-reply";
    }

}
