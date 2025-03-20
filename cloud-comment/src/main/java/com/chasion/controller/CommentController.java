package com.chasion.controller;

import com.chasion.entity.CommentDTO;
import com.chasion.entity.DiscussPostDTO;
import com.chasion.entity.Event;
import com.chasion.event.EventProducer;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.service.CommentService;
import com.chasion.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.chasion.utils.CommunityConstant.*;

@RestController
@RequestMapping("/commentService")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    // 获取comment list
    // int entityType, int entityId, int offset, int limit
    @GetMapping("/getList")
    public ResultData<List<CommentDTO>> getCommentList(@RequestParam("entityType")int entityType,
                                                       @RequestParam("entityId")int entityId,
                                                       @RequestParam("offset") int offset,
                                                       @RequestParam("limit")int limit) {
        ResultData<List<CommentDTO>> result = new ResultData<>();
        List<CommentDTO> commentDTOList = commentService.getCommentDTOList(entityType, entityId, offset, limit);
        if (commentDTOList != null && !commentDTOList.isEmpty()) {
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
    public ResultData<CommentDTO> addComment(@RequestParam("userId") int userId,
                                             @RequestParam("postId") int postId,
                                             @RequestParam("targetUserId") int targetUserId,
                                             @RequestBody CommentDTO comment) {
        ResultData<CommentDTO> result = new ResultData<>();
        int row = commentService.addComment(comment);

        if (row > 0) {
            result.setCode(ReturnCodeEnum.RC200.getCode());
            result.setMessage(ReturnCodeEnum.RC200.getMessage());
            // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(userId)
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", postId);
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            event.setEntityUserId(targetUserId);
        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){
//            System.out.println("comment id:" + comment.getId());
            // 这里不能用自增主键，带事务的，
            CommentDTO commentDTO = commentService.getCommentById(comment.getEntityId());
            event.setEntityUserId(commentDTO.getUserId());
        }

        eventProducer.fireEvent(event);
        // 评论给帖子时触发
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(userId)
                    .setEntityId(postId)
                    .setEntityType(ENTITY_TYPE_POST);
            eventProducer.fireEvent(event);
            // 计算帖子分数
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey, postId);
        }
        }else {
            result.setCode(ReturnCodeEnum.RC999.getCode());
            result.setMessage(ReturnCodeEnum.RC999.getMessage());
        }
        return result;
    }
}
