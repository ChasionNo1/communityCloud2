package com.chasion.controller;

import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/userService")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/like")
    public ResultData<HashMap<String, Object>> like(@RequestParam("userId") int userId,
                                   @RequestParam("entityType") int entityType,
                                   @RequestParam("entityId") int entityId,
                                   @RequestParam("entityUserId")int entityUserId){
        System.out.println("step into ----------");
        // 点赞
        likeService.like(userId, entityType, entityId, entityUserId);
        // 获取：entityLikeCount和entityLikeStatus
        HashMap<String, Object> map = new HashMap<>();
        map.put("entityLikeCount", likeService.getEntityLikeCount(entityType, entityId));
        map.put("entityLikeStatus", likeService.getEntityLikeStatus(userId, entityType, entityId));
        ResultData<HashMap<String, Object>> resultData = new ResultData<>();
        resultData.setData(map);
        resultData.setCode(ReturnCodeEnum.RC202.getCode());
        resultData.setMessage(ReturnCodeEnum.RC202.getMessage());
        return resultData;

    }
}
