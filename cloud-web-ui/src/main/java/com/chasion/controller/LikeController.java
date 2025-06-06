package com.chasion.controller;

import com.chasion.annotation.LoginRequired;
import com.chasion.apis.UserFeignApi;
import com.chasion.entity.UserDTO;
import com.chasion.resp.ResultData;
import com.chasion.utils.CommunityUtil;
import com.chasion.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
@RefreshScope
public class LikeController {
    /**
     * 点赞和取消点赞
     *
     * */

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserFeignApi userFeignApi;

    @LoginRequired
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){
        UserDTO user = hostHolder.getUser();
        // 这里调用usefeign
        ResultData<HashMap<String, Object>> resultData =
                userFeignApi.like(user.getId(), entityType, entityId, entityUserId, postId);
        // 触发点赞事件
//        if (entityLikeStatus == 1){
//            Event event = new Event()
//                    .setTopic(TOPIC_LIKE)
//                    .setUserId(hostHolder.getUser().getId())
//                    .setEntityType(entityType)
//                    .setEntityId(entityId)
//                    .setEntityUserId(entityUserId)
//                    .setData("postId", postId)
//                    ;
//            eventProducer.fireEvent(event);
//        }
//
//        // 计算帖子分数
//        if (entityType == ENTITY_TYPE_POST){
//            String postScoreKey = RedisKeyUtil.getPostScoreKey();
//            redisTemplate.opsForSet().add(postScoreKey, postId);
//        }

        HashMap<String, String> map = userFeignApi.getLikeCount(user.getId(), entityType, entityId).getData();
        HashMap<String, Object> res = new HashMap<>();
        res.put("entityLikeCount", map.get("entityLikeCount"));
        res.put("entityLikeStatus", map.get("entityLikeStatus"));
        return CommunityUtil.getJSONString(0, resultData.getMessage(), res);

    }

}
