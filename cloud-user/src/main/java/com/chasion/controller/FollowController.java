package com.chasion.controller;

import com.chasion.entity.Event;
import com.chasion.entity.FollowListDTO;
import com.chasion.event.EventProducer;
import com.chasion.resp.ResultData;
import com.chasion.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static com.chasion.utils.CommunityConstant.TOPIC_FOLLOW;

@RestController
@RequestMapping("/userService")
@RefreshScope
public class FollowController {

    /**
     * 处理用户的关注和取消关注动作
     */

    @Autowired
    private FollowService followService;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/follow/user")
    public ResultData<Object> follow(@RequestParam("userId") int userId,
                                     @RequestParam("entityType") int entityType,
                                     @RequestParam("entityId") int entityId) {
        followService.follow(userId, entityType, entityId);
        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(userId)
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return new ResultData<>();
    }

    @PostMapping("/unfollow/user")
    public ResultData<Object> unfollow(@RequestParam("userId") int userId,
                                       @RequestParam("entityType") int entityType,
                                       @RequestParam("entityId") int entityId){
        followService.unfollow(userId, entityType, entityId);
        return new ResultData<>();
    }

    // 获取关注的人数量
//    followService.getFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
    @GetMapping("/get/followeeCount")
    public long getFolloweeCount(@RequestParam("userId") int userId, @RequestParam("entityType") int entityType){
        return followService.getFolloweeCount(userId, entityType);
    }

    // 获取粉丝的数量
    @GetMapping("/get/followerCount")
    public long getFollowerCount(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId){
        return followService.getFollowerCount(entityType, entityId);
    }

    // 是否存在关注关系   int userId, int entityType, int entityId
    @GetMapping("/get/isFollowed")
    public boolean getIsFollowed(@RequestParam("userId") int userId,
                                 @RequestParam("entityType") int entityType,
                                 @RequestParam("entityId") int entityId){
        return followService.isFollowed(userId, entityType, entityId);
    }

    // 获取关注列表：followService.getFolloweeList(userId, CommunityConstant.ENTITY_TYPE_USER, page.getOffset(), page.getLimit());
    @GetMapping("/get/followeeList")
    public ResultData<List<FollowListDTO>> getFolloweeList(@RequestParam("userId") int userId,
                                                           @RequestParam("entityType") int entityType,
                                                           @RequestParam("offset") int offset,
                                                           @RequestParam("limit") int limit){
        List<FollowListDTO> followeeList = followService.getFolloweeList(userId, entityType, offset, limit);
        ResultData<List<FollowListDTO>> resultData = new ResultData<>();
        resultData.setData(followeeList);
        return resultData;
    }

    // 获取粉丝列表：public List<Map<String, Object>> getFollowerList(int entityType, int entityId, int offset, int limit)
    @GetMapping("/get/followerList")
    public ResultData<List<FollowListDTO>> getFollowerList(@RequestParam("entityType") int entityType,
                                                                 @RequestParam("entityId") int entityId,
                                                                 @RequestParam("offset") int offset,
                                                                 @RequestParam("limit") int limit){
        List<FollowListDTO> followerList = followService.getFollowerList(entityType, entityId, offset, limit);
        ResultData<List<FollowListDTO>> resultData = new ResultData<>();
        resultData.setData(followerList);
        return resultData;
    }

}
