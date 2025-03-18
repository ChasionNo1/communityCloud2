package com.chasion.controller;

import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        // 点赞
        likeService.like(userId, entityType, entityId, entityUserId);
        // 获取：entityLikeCount和entityLikeStatus
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("entityLikeCount", likeService.getEntityLikeCount(entityType, entityId));
//        map.put("entityLikeStatus", likeService.getEntityLikeStatus(userId, entityType, entityId));
        ResultData<HashMap<String, Object>> resultData = new ResultData<>();
//        resultData.setData(map);
        resultData.setCode(ReturnCodeEnum.RC202.getCode());
        resultData.setMessage(ReturnCodeEnum.RC202.getMessage());
        return resultData;
    }

    @GetMapping("/get/likeCount")
    public ResultData<HashMap<String, String>> getLikeCount(@RequestParam("userId") int userId,
                                                            @RequestParam("entityType") int entityType,
                                                            @RequestParam("entityId") int entityId){
        HashMap<String, String> map = new HashMap<>();
        map.put("entityLikeCount", likeService.getEntityLikeCount(entityType, entityId) + "");
        map.put("entityLikeStatus", likeService.getEntityLikeStatus(userId, entityType, entityId) + "");
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
