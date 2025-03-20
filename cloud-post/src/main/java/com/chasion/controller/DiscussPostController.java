package com.chasion.controller;

import com.chasion.entity.DiscussPostDTO;
import com.chasion.entity.Page;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.entity.*;
//import com.chasion.cloudpost.event.EventProducer;
//import com.chasion.cloudpost.service.CommentService;
import com.chasion.service.DiscussPostService;
//import com.chasion.cloudpost.service.LikeService;
//import com.chasion.cloudpost.service.UserService;
//import com.chasion.cloudpost.util.CommunityConstant;
//import com.chasion.cloudpost.util.CommunityUtil;
//import com.chasion.cloudpost.util.HostHolder;
//import com.chasion.cloudpost.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/discussPost")
public class DiscussPostController{

    @Autowired
    private DiscussPostService discussPostService;

    // 查询帖子总数，用于分页查询
    @GetMapping("/get/count")
    public int discussPostCount(@RequestParam("userId") int userId){
        return discussPostService.findDiscussPostRows(userId);
    }

    // 返回首页帖子数据 DTO
    @PostMapping("/get/list")
    public ResultData<List<DiscussPostDTO>> discussPosts(@RequestParam("userId") int userId, @RequestBody Page page, @RequestParam("orderMode") int orderMode) {
        // 返回要查询的数据，带分页信息，定义一个返回数据接口
        // 查询分页数据
        ResultData<List<DiscussPostDTO>> resultData = new ResultData<>();
        List<DiscussPostDTO> postDTOList = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), orderMode);
        if (postDTOList != null && !postDTOList.isEmpty()){
            resultData.setData(postDTOList);
        }else {
            resultData.setCode(ReturnCodeEnum.BUSINESS_ERROR.getCode());
            resultData.setMessage(ReturnCodeEnum.BUSINESS_ERROR.getMessage());
        }
        return resultData;
    }

    // 增加帖子
    @PostMapping("/add/discussPost")
    public ResultData<String> addDiscussPost(@RequestParam("userId") int userId, @RequestParam("title") String title, @RequestParam("content") String content) {
        discussPostService.addDiscussPost(userId, title, content);
        return new ResultData<>();
    }

    // 根据id查找帖子
    @GetMapping("/get/discuss/{id}")
    public DiscussPostDTO getDiscussPost(@PathVariable("id") int id) {
        return discussPostService.findDiscussPostById(id);
    }

    // 更新帖子的评论数量
    @PostMapping("/update/commentCount")
    public int updateCommentCount(@RequestParam("postId") int postId, @RequestParam("commentCount") int commentCount) {
        return discussPostService.updateCommentCount(postId, commentCount);
    }




//    @Autowired
//    private HostHolder hostHolder;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private CommentService commentService;
//
//    @Autowired
//    private LikeService likeService;
//
//    @Autowired
//    private EventProducer eventProducer;
//
//    @Autowired
//    private RedisTemplate redisTemplate;

    // 增加帖子，返回json字符串
//    @RequestMapping(value = "/add", method = RequestMethod.POST)
//    @ResponseBody
//    public String addDiscussPost(String title, String content) {
//        // 封装discussPost
//        User user = hostHolder.getUser();
//        if (user == null) {
//            return CommunityUtil.getJSONString(403, "您还没有登录");
//        }
//        DiscussPost discussPost = new DiscussPost();
//        // 获取作者的id
//        int userId = user.getId();
//        discussPost.setUserId(userId);
//        discussPost.setTitle(title);
//        discussPost.setContent(content);
//        discussPost.setCreateTime(new Date());
//        discussPostService.addDiscussPost(discussPost);
//
//        // 触发发帖事件，将新发布的帖子存到es服务器中
//        Event event = new Event()
//                .setTopic(TOPIC_PUBLISH)
//                .setUserId(userId)
//                .setEntityType(ENTITY_TYPE_POST)
//                .setEntityId(discussPost.getId());
//
//        eventProducer.fireEvent(event);
//
//        // 计算帖子分数
//        String postScoreKey = RedisKeyUtil.getPostScoreKey();
//        redisTemplate.opsForSet().add(postScoreKey, discussPost.getId());
//        return CommunityUtil.getJSONString(0, "发布成功");
//
//    }
    // 获取帖子详情页面
//    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
//    public String getDiscussPostDetail(@PathVariable int id, Model model, Page page) {
//        // 显示帖子的内容: 标题
//        DiscussPost post = discussPostService.findDiscussPostById(id);
//        if (post == null) {
//            return CommunityUtil.getJSONString(404, "post is not found");
//        }
//        int userId = post.getUserId();
//        // 一般说来这儿是不为空的
//        User user = userService.findUserById(userId);
//        model.addAttribute("post", post);
//        model.addAttribute("user", user);
//        // 帖子的赞数量
//        long likeCount = likeService.getEntityLikeCount(ENTITY_TYPE_POST, post.getId());
//        model.addAttribute("likeCount", likeCount);
//        // 点赞状态
//        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.getEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, post.getId());
//        model.addAttribute("likeStatus", likeStatus);
////        System.out.println("-----------------帖子----------------------------");
////        System.out.println("likeCount:" + likeCount);
////        System.out.println("likeStatus:" + likeStatus);
//        // 设置评论分页
//        page.setLimit(5);
//        page.setPath("/discuss/" + id);
//        page.setRows(post.getCommentCount());
//
//        // 获取帖子的评论列表
//        List<Comment> commentList = commentService.getComments(CommunityConstant.ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
//        // 封装信息，评论者信息，评论内容信息，以及评论的回复信息
//        List<Map<String, Object>> commentVolist = new ArrayList<Map<String, Object>>();
//        if (commentList != null && !commentList.isEmpty()) {
//            for (Comment comment : commentList) {
//                HashMap<String, Object> commentVo = new HashMap<>();
//                // 评论内容
//                commentVo.put("comment", comment);
//                // 评论者
//                commentVo.put("user", userService.findUserById(comment.getUserId()));
//                // 评论的赞数量和状态
//                likeCount = likeService.getEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
//                likeStatus = hostHolder.getUser() == null ? 0 : likeService.getEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
//                commentVo.put("likeStatus", likeStatus);
//                commentVo.put("likeCount", likeCount);
////                System.out.println("-------------------评论----------------------------");
////                System.out.println("likeCount:" + likeCount);
////                System.out.println("likeStatus:" + likeStatus);
//                // 评论的回复信息
//                // 查询所有回复信息
//                List<Comment> replyList = commentService.getComments(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
//                // 有对帖子的comment，有对comment的comment
//                // 回复的Vo列表
//                List<Map<String, Object>> replyVolist = new ArrayList<>();
//                if (replyList != null && !replyList.isEmpty()) {
//                    for (Comment reply : replyList) {
//                        HashMap<String, Object> replyVo = new HashMap<>();
//                        replyVo.put("reply", reply);
////                        System.out.println("reply = " + reply.getContent());
//                        replyVo.put("user", userService.findUserById(reply.getUserId()));
//                        // 回复目标, targetId ---> target user id
//                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
////                        System.out.println("target id: " + reply.getTargetId());
//                        replyVo.put("target", target);
//                        // 回复的点赞数量和状态
//                        likeCount = likeService.getEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
//                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.getEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
//                        replyVo.put("likeStatus", likeStatus);
//                        replyVo.put("likeCount", likeCount);
//                        replyVolist.add(replyVo);
//                    }
//                }
//                commentVo.put("replies", replyVolist);
//                // 回复数量
//                int replyCount = commentService.getCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
//                commentVo.put("replyCount", replyCount);
//                commentVolist.add(commentVo);
//            }
//        }
//        model.addAttribute("comments", commentVolist);
//
//        return "/site/discuss-detail";
//    }

    /**
     * 删除帖子的功能
     *  管理员才有的功能
     * */
//    @RequestMapping(path = "/delete", method = RequestMethod.POST)
//    @ResponseBody
//    public String setDelete(int id){
//        User user = hostHolder.getUser();
//        discussPostService.updateStatus(id, 2);
//        // 触发事件
//        Event event = new Event()
//                .setTopic(TOPIC_DELETE)
//                .setUserId(user.getId())
//                .setEntityType(ENTITY_TYPE_POST)
//                .setEntityType(id);
//        eventProducer.fireEvent(event);
//        return CommunityUtil.getJSONString(0);
//    }


    // 置顶
    // 0是普通，1是置顶
//    @RequestMapping(value = "/top", method = RequestMethod.POST)
//    @ResponseBody
//    public String setTop(int id) {
//        User user = hostHolder.getUser();
//        discussPostService.updateType(id, 1);
//        // 同步帖子数据到es中
//        // 触发事件
//        Event event = new Event()
//                .setTopic(TOPIC_PUBLISH)
//                .setUserId(user.getId())
//                .setEntityType(ENTITY_TYPE_POST)
//                .setEntityType(id);
//        eventProducer.fireEvent(event);
//        return CommunityUtil.getJSONString(0);
//    }

//    // 加精
//    // 0是正常， 1是精华，2是拉黑
//    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
//    @ResponseBody
//    public String setWonderful(int id) {
//        User user = hostHolder.getUser();
//        discussPostService.updateStatus(id, 1);
//        // 同步到es中
//        Event event = new Event()
//                .setTopic(TOPIC_PUBLISH)
//                .setUserId(user.getId())
//                .setEntityType(ENTITY_TYPE_POST)
//                .setEntityType(id);
//        eventProducer.fireEvent(event);
//
//        // 计算帖子分数
//        String postScoreKey = RedisKeyUtil.getPostScoreKey();
//        redisTemplate.opsForSet().add(postScoreKey, id);
//
//        return CommunityUtil.getJSONString(0);
//    }

}
