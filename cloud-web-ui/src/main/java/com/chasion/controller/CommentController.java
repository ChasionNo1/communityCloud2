package com.chasion.controller;

import com.chasion.apis.CommentFeignApi;
import com.chasion.apis.DiscussPostFeignApi;
import com.chasion.entity.CommentDTO;
import com.chasion.entity.DiscussPostDTO;
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

    @Autowired
    private DiscussPostFeignApi discussPostFeignApi;

    /**
     * 对帖子发表评论，对评论发表回复
     * */

    // 评论和回复用的是一个实体，只是实体类型不一样
    @RequestMapping(value = "/comment//add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Model model, CommentDTO comment) {
        int userId = hostHolder.getUser().getId();
        comment.setUserId(userId);
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        DiscussPostDTO target = discussPostFeignApi.getDiscussPost(comment.getEntityId());
        commentFeignApi.addComment(userId, discussPostId, target.getUserId(), comment);
//        // 更新帖子的评论数量?  增加数量
        discussPostFeignApi.updateCommentCount(discussPostId, 1);


        return "redirect:/discuss/" + discussPostId;
    }
}
