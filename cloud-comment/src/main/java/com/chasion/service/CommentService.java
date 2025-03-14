package com.chasion.service;

import com.chasion.dao.CommentMapper;
import com.chasion.entity.Comment;
import com.chasion.entity.CommentDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    // 获取帖子评论的列表
    // CommunityConstant.ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit()
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public List<CommentDTO> getCommentDTOList(int entityType, int entityId, int offset, int limit) {
        List<Comment> commentList = commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
        List<CommentDTO> commentDTOList = new ArrayList<CommentDTO>();
        if (commentList != null && !commentList.isEmpty()) {
            for (Comment comment : commentList) {
                CommentDTO commentDTO = new CommentDTO();
                // 这里一致就直接复制了
                BeanUtils.copyProperties(comment, commentDTO);
                commentDTOList.add(commentDTO);
            }
        }
        return commentDTOList;
    }

    // 获取comment的数量
    public int getCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    // 添加comment
    public int addComment(CommentDTO commentDTO) {
        Comment comment = new Comment();
        if (commentDTO == null) {
            return 0;
        }else {
            BeanUtils.copyProperties(commentDTO, comment);
        }
        return commentMapper.insertComment(comment);
    }

}
