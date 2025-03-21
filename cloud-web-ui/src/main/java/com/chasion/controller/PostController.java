package com.chasion.controller;

import com.chasion.annotation.LoginRequired;
import com.chasion.apis.CommentFeignApi;
import com.chasion.apis.DiscussPostFeignApi;
import com.chasion.apis.UserFeignApi;
import com.chasion.entity.CommentDTO;
import com.chasion.entity.DiscussPostDTO;
import com.chasion.entity.Page;
import com.chasion.entity.UserDTO;
import com.chasion.utils.CommunityUtil;
import com.chasion.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chasion.utils.CommunityConstant.ENTITY_TYPE_COMMENT;
import static com.chasion.utils.CommunityConstant.ENTITY_TYPE_POST;

@Controller
public class PostController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostFeignApi discussPostFeignApi;

    @Autowired
    private UserFeignApi userFeignApi;

    @Autowired
    private CommentFeignApi commentFeignApi;

    /**
     * 帖子模块
     * */
    // 处理发布帖子的异步请求
    // 从页面接收数据，做一下过滤，然后添加到数据库中，然后刷新一下页面
    @PostMapping("/discuss/add")
    @ResponseBody
    @LoginRequired
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

    // 获取帖子详情页面
    @RequestMapping(value = "/discuss/{id}", method = RequestMethod.GET)
    public String getDiscussPostDetail(@PathVariable int id, Model model, Page page) {
        // 显示帖子的内容: 标题
        DiscussPostDTO post = discussPostFeignApi.getDiscussPost(id);
        if (post == null) {
            return CommunityUtil.getJSONString(404, "post is not found");
        }
        int userId = post.getUserId();
        // 一般说来这儿是不为空的
        UserDTO user = userFeignApi.findUserById(userId);
        model.addAttribute("post", post);
        model.addAttribute("user", user);
        // 这里有问题，如果没登陆访问帖子详情的话，这里会有问题
        // required=false
        HashMap<String, String> map = new HashMap<>();
        int entityLikeStatus = 0;
        if (hostHolder.getUser() != null) {
             map = userFeignApi.getLikeCount(hostHolder.getUser().getId(), ENTITY_TYPE_POST, post.getId()).getData();
             entityLikeStatus = Integer.parseInt(map.get("entityLikeStatus"));

        }else {
        }
        Object likeCount = map.get("entityLikeCount");
        // 帖子的赞数量
        // 这里有问题，类型转换的问题：

//        System.out.println("------------:" + likeCount);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态，这里也进行了判断，但是在这之前还有hostholder的使用
        int likeStatus = hostHolder.getUser() == null ? 0 : entityLikeStatus;
//        System.out.println(likeStatus);
        model.addAttribute("likeStatus", likeStatus);
//        System.out.println("-----------------帖子----------------------------");
//        System.out.println("likeCount:" + likeCount);
//        System.out.println("likeStatus:" + likeStatus);
        // 设置评论分页
        page.setLimit(5);
        page.setPath("/discuss/" + id);
//        System.out.println("post rows = " + post.getCommentCount());
        // 帖子的评论数量
        page.setRows(post.getCommentCount());

        // 获取帖子的评论列表
        List<CommentDTO> commentList = commentFeignApi.getCommentList(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit()).getData();
        // 封装信息，评论者信息，评论内容信息，以及评论的回复信息
        List<Map<String, Object>> commentVolist = new ArrayList<>();
        if (commentList != null && !commentList.isEmpty()) {
            for (CommentDTO comment : commentList) {
                HashMap<String, Object> commentVo = new HashMap<>();
                // 评论内容
                commentVo.put("comment", comment);
                // 评论者
                commentVo.put("user", userFeignApi.findUserById(comment.getUserId()));
                // 评论的赞数量和状态
                // 这地方也是的
                HashMap<String, String> commentLikeData = new HashMap<>();
                int commentLikeStatus = 0;
                if (hostHolder.getUser() != null) {
                    commentLikeData = userFeignApi.getLikeCount(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId()).getData();
                    commentLikeStatus = Integer.parseInt(commentLikeData.get("entityLikeStatus"));
                }else {

                }

                likeCount = commentLikeData.get("entityLikeCount");
                likeStatus = hostHolder.getUser() == null ? 0 : commentLikeStatus;
                commentVo.put("likeStatus", likeStatus);
                commentVo.put("likeCount", likeCount);
//                System.out.println("-------------------评论----------------------------");
//                System.out.println("likeCount:" + likeCount);
//                System.out.println("likeStatus:" + likeStatus);
                // 评论的回复信息
                // 查询所有回复信息
                List<CommentDTO> replyList = commentFeignApi.getCommentList(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE).getData();
                // 有对帖子的comment，有对comment的comment
                // 回复的Vo列表
                List<Map<String, Object>> replyVolist = new ArrayList<>();
                if (replyList != null && !replyList.isEmpty()) {
                    for (CommentDTO reply : replyList) {
                        HashMap<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
//                        System.out.println("reply = " + reply.getContent());
                        replyVo.put("user", userFeignApi.findUserById(reply.getUserId()));
                        // 回复目标, targetId ---> target user id
                        UserDTO target = reply.getTargetId() == 0 ? null : userFeignApi.findUserById(reply.getTargetId());
//                        System.out.println("target id: " + reply.getTargetId());
                        replyVo.put("target", target);
                        HashMap<String, String> replyCountData = new HashMap<>();
                        int replyLikeStatus = 0;
                        if (hostHolder.getUser() != null) {
                            replyCountData = userFeignApi.getLikeCount(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId()).getData();
                            replyLikeStatus = Integer.parseInt(replyCountData.get("entityLikeStatus"));
                        }
                        // 回复的点赞数量和状态
                        likeCount =  replyCountData.get("entityLikeCount");
                        likeStatus = hostHolder.getUser() == null ? 0 : replyLikeStatus;
//                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.getEntityLikeStatus();
                        replyVo.put("likeStatus", likeStatus);
                        replyVo.put("likeCount", likeCount);
                        replyVolist.add(replyVo);
                    }
                }
                commentVo.put("replies", replyVolist);
                // 回复数量
                int replyCount = commentFeignApi.getCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVolist.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVolist);

        return "/site/discuss-detail";
    }

    // 添加评论



}
