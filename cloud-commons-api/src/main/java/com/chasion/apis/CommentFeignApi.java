package com.chasion.apis;

import com.chasion.entity.CommentDTO;
import com.chasion.entity.DiscussPostDTO;
import com.chasion.resp.ResultData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "cloud-comment", path = "/commentService")
public interface CommentFeignApi {

    @GetMapping("/getList")
    public ResultData<List<CommentDTO>> getCommentList(@RequestParam("entityType")int entityType,
                                                       @RequestParam("entityId")int entityId,
                                                       @RequestParam("offset") int offset,
                                                       @RequestParam("limit")int limit);

    @GetMapping("/getCount")
    public int getCommentCount(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId);

    @PostMapping("/add")
    public ResultData<CommentDTO> addComment(@RequestParam("userId") int userId,
                                             @RequestParam("postId") int postId,
                                             @RequestParam("targetUserId") int targetUserId,
                                             @RequestBody CommentDTO comment);
}
