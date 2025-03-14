package com.chasion.controller;

import com.chasion.apis.CommentFeignApi;
import com.chasion.entity.CommentDTO;
import com.chasion.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
public class CommentController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentFeignApi commentFeignApi;

    /**
     * 对帖子发表评论，对评论发表回复
     * */

    // 评论和回复用的是一个实体，只是实体类型不一样
    @RequestMapping(value = "/add/comment/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Model model, CommentDTO comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentFeignApi.addComment(comment);
        // 触发评论事件
//        Event event = new Event()
//                .setTopic(TOPIC_COMMENT)
//                .setUserId(hostHolder.getUser().getId())
//                .setEntityType(comment.getEntityType())
//                .setEntityId(comment.getEntityId())
//                .setData("postId", discussPostId);
//        if (comment.getEntityType() == ENTITY_TYPE_POST){
//            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
//            event.setEntityUserId(target.getUserId());
//        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){
////            System.out.println("comment id:" + comment.getId());
//            // 这里不能用自增主键，带事务的，
//            Comment target = commentService.getCommentById(comment.getEntityId());
//            event.setEntityUserId(target.getUserId());
//        }

//        eventProducer.fireEvent(event);
//        // 评论给帖子时触发
//        if (comment.getEntityType() == ENTITY_TYPE_POST){
//            event = new Event()
//                    .setTopic(TOPIC_PUBLISH)
//                    .setUserId(hostHolder.getUser().getId())
//                    .setEntityId(discussPostId)
//                    .setEntityType(ENTITY_TYPE_POST);
//            eventProducer.fireEvent(event);
//            // 计算帖子分数
//            String postScoreKey = RedisKeyUtil.getPostScoreKey();
//            redisTemplate.opsForSet().add(postScoreKey, discussPostId);
//        }

        return "redirect:/discuss/" + discussPostId;
    }
}
