package com.chasion.service;

import com.chasion.entity.DiscussPostDTO;
import com.chasion.dao.DiscussPostMapper;
import com.chasion.entity.DiscussPost;
import com.chasion.entity.UserDTO;
import com.chasion.utils.HostHolder;
import com.chasion.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;


    // 查询分页数据  这里带userId是为了后面查看个人用户发的帖子
    public List<DiscussPostDTO> findDiscussPosts(int userId, int offset, int limit, int orderMode){
        // 需要将数据转为DTO
        List<DiscussPost> postList = discussPostMapper.selectDiscussPost(userId, offset, limit, orderMode);
        List<DiscussPostDTO> postDTOList = new ArrayList<>();
        if (postList != null && !postList.isEmpty()){
            // 开始遍历
            for (DiscussPost post : postList){
                DiscussPostDTO postDTO = new DiscussPostDTO();
                postDTO.setId(post.getId());
                postDTO.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
                postDTO.setContent(HtmlUtils.htmlEscape(post.getContent()));
                postDTO.setUserId(post.getUserId());
                postDTO.setCommentCount(post.getCommentCount());
                postDTO.setStatus(post.getStatus());
                postDTO.setScore(post.getScore());
                postDTO.setType(post.getType());
                postDTO.setCreateTime(post.getCreateTime());
                postDTOList.add(postDTO);
            }
        }
        return postDTOList;
    }
    // 查询帖子总数
    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    // 发布帖子
    public int addDiscussPost(DiscussPost discussPost){
        if (discussPost == null){
            throw new IllegalArgumentException("discussPost is null");
        }
        // 转义html标签
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 过滤敏感词
//        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
//        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    // 查询一个帖子
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    // 更新帖子的评论数量
    public int updateCommentCount(int id, int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    // 更新帖子类型
    public int updateType(int id, int type){
        return discussPostMapper.updateType(id,type);
    }

    public int updateStatus(int id, int status){
        return discussPostMapper.updateStatus(id,status);
    }

    public int updateDiscussPostScore(int id, double score){
        return discussPostMapper.updateDiscussPostScore(id,score);
    }

    // 增加帖子
    public int addDiscussPost(int userId, String title, String content){
        DiscussPost discussPost = new DiscussPost();
        // 获取发表人的id
        discussPost.setUserId(userId);
        discussPost.setTitle(sensitiveFilter.filter(HtmlUtils.htmlEscape(title)));
        discussPost.setContent(sensitiveFilter.filter(HtmlUtils.htmlEscape(content)));
        discussPost.setCreateTime(new Date());
        return discussPostMapper.insertDiscussPost(discussPost);

    }

}
