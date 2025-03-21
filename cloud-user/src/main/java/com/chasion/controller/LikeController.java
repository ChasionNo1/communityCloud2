package com.chasion.controller;

import com.chasion.entity.Event;
import com.chasion.event.EventProducer;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.service.LikeService;
import com.chasion.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import static com.chasion.utils.CommunityConstant.ENTITY_TYPE_POST;
import static com.chasion.utils.CommunityConstant.TOPIC_LIKE;

@RestController
@RequestMapping("/userService")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/like")
    public ResultData<HashMap<String, Object>> like(@RequestParam("userId") int userId,
                                   @RequestParam("entityType") int entityType,
                                   @RequestParam("entityId") int entityId,
                                   @RequestParam("entityUserId")int entityUserId,
                                                    @RequestParam("postId") int postId){
        // 点赞
        likeService.like(userId, entityType, entityId, entityUserId);
        int entityLikeStatus = likeService.getEntityLikeStatus(userId, entityType, entityId);
        // 触发点赞事件
        if (entityLikeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(userId)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId)
                    ;
            eventProducer.fireEvent(event);
        }

        // 计算帖子分数
        if (entityType == ENTITY_TYPE_POST){
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey, postId);
        }


        ResultData<HashMap<String, Object>> resultData = new ResultData<>();
//        resultData.setData(map);
        resultData.setCode(ReturnCodeEnum.RC200.getCode());
        resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
        return resultData;
    }

    @GetMapping("/get/likeCount")
    public ResultData<HashMap<String, String>> getLikeCount(@RequestParam(value = "userId", required = false) Integer userId,
                                                            @RequestParam("entityType") int entityType,
                                                            @RequestParam("entityId") int entityId){
        HashMap<String, String> map = new HashMap<>();
        map.put("entityLikeCount", likeService.getEntityLikeCount(entityType, entityId) + "");
        // 如果没有用户登录，就不需要展示点赞情况
        if (userId == null){
            map.put("entityLikeCount", "0");
        }else {
            map.put("entityLikeStatus", likeService.getEntityLikeStatus(userId, entityType, entityId) + "");
        }
        System.out.println("step into ----------");
        System.out.println(map.get("entityLikeCount"));
        System.out.println(map.get("entityLikeStatus"));
        ResultData<HashMap<String, String>> resultData = new ResultData<>();
        resultData.setData(map);
        resultData.setCode(ReturnCodeEnum.RC200.getCode());
        resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
        return resultData;
    }

    // 统计用户个人收到的赞数量
    @GetMapping("/get/user/likeCount")
    public int getUserLikeCount(@RequestParam("userId") int userId){
        return likeService.getUserLikeCount(userId);
    }
}
