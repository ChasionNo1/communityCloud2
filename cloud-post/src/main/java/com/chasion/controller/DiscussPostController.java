package com.chasion.controller;

import com.chasion.entity.DiscussPostDTO;
import com.chasion.entity.Page;
import com.chasion.event.EventProducer;
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
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.chasion.utils.CommunityConstant.ENTITY_TYPE_POST;
import static com.chasion.utils.CommunityConstant.TOPIC_PUBLISH;

@RestController
@RequestMapping("/discussPost")
@RefreshScope
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

    // 置顶帖子
    // int id, int type
    // 0是普通，1是置顶
    @PostMapping("/update/discuss/type")
    public ResultData<Integer> setTop(@RequestParam("id") int id, @RequestParam("type") int type, @RequestParam("userId") int userId) {
        int row = discussPostService.updateType(id, type, userId);
        ResultData<Integer> resultData = new ResultData<>();
        resultData.setCode(ReturnCodeEnum.RC200.getCode());
        resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
        resultData.setData(row);
        return resultData;
    }

    // 加精帖子,修改帖子的状态
    @PostMapping("/update/discuss/wonderful")
    public ResultData<Integer> setStatus(@RequestParam("id") int id, @RequestParam("status") int status, @RequestParam("userId") int userId) {
        int row = discussPostService.setDiscussPostWonderful(id, status, userId);
        ResultData<Integer> resultData = new ResultData<>();
        resultData.setCode(ReturnCodeEnum.RC200.getCode());
        resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
        resultData.setData(row);
        return resultData;
    }
    // 删除帖子
    @PostMapping("/update/discuss/delete")
    public ResultData<Integer> setDelete(@RequestParam("id") int id, @RequestParam("status") int status, @RequestParam("userId") int userId) {
        int row = discussPostService.deleteDiscussPost(id, status, userId);
        ResultData<Integer> resultData = new ResultData<>();
        resultData.setCode(ReturnCodeEnum.RC200.getCode());
        resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
        resultData.setData(row);
        return resultData;
    }






}
