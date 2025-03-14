package com.chasion.controller;

import com.chasion.entity.CommentDTO;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commentService")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 获取comment list
    // int entityType, int entityId, int offset, int limit
    @GetMapping("/getList")
    public ResultData<List<CommentDTO>> getCommentList(@RequestParam("entityType")int entityType,
                                                       @RequestParam("entityId")int entityId,
                                                       @RequestParam("offset") int offset,
                                                       @RequestParam("limit")int limit) {
        ResultData<List<CommentDTO>> result = new ResultData<>();
        List<CommentDTO> commentDTOList = commentService.getCommentDTOList(entityType, entityId, offset, limit);
        if (commentDTOList != null && commentDTOList.size() > 0) {
            result.setData(commentDTOList);
            result.setCode(ReturnCodeEnum.RC200.getCode());
            result.setMessage(ReturnCodeEnum.RC200.getMessage());
        }
        return result;
    }

    // 获取comment的数量
    // int entityType, int entityId
    @GetMapping("/getCount")
    public int getCommentCount(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId) {
        return commentService.getCommentCount(entityType, entityId);
    }


    // 添加comment
    @PostMapping("/add")
    public ResultData<CommentDTO> addComment(@RequestBody CommentDTO comment) {
        ResultData<CommentDTO> result = new ResultData<>();
        int row = commentService.addComment(comment);
        if (row > 0) {
            result.setCode(ReturnCodeEnum.RC200.getCode());
            result.setMessage(ReturnCodeEnum.RC200.getMessage());
        }else {
            result.setCode(ReturnCodeEnum.RC999.getCode());
            result.setMessage(ReturnCodeEnum.RC999.getMessage());
        }
        return result;
    }
}
