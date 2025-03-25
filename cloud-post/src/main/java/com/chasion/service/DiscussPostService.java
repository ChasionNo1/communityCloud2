package com.chasion.service;

import com.chasion.entity.DiscussPostDTO;
import com.chasion.dao.DiscussPostMapper;
import com.chasion.entity.DiscussPost;
import com.chasion.entity.Event;
import com.chasion.entity.UserDTO;
import com.chasion.event.EventProducer;
import com.chasion.utils.HostHolder;
import com.chasion.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.chasion.utils.CommunityConstant.*;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private EventProducer eventProducer;


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
    // 查询某个用户发布的帖子总数
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
    public DiscussPostDTO findDiscussPostById(int id){
        DiscussPostDTO discussPostDTO = new DiscussPostDTO();
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(id);
        if (discussPost != null){
            discussPostDTO.setId(discussPost.getId());
            discussPostDTO.setContent(discussPost.getContent());
            discussPostDTO.setTitle(discussPost.getTitle());
            discussPostDTO.setUserId(discussPost.getUserId());
            discussPostDTO.setType(discussPost.getType());
            discussPostDTO.setStatus(discussPost.getStatus());
            discussPostDTO.setCreateTime(discussPost.getCreateTime());
            discussPostDTO.setCommentCount(discussPost.getCommentCount());
            discussPostDTO.setScore(discussPost.getScore());
        }else {
            throw new IllegalArgumentException("discussPost is null");
        }
        return discussPostDTO;
    }

    // 更新帖子的评论数量
    public int updateCommentCount(int id, int commentCount){
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(id);
        return discussPostMapper.updateCommentCount(id,commentCount+discussPost.getCommentCount());
    }

    // 更新帖子类型
    public int updateType(int id, int type, int userId){
        // 同步帖子数据到es中
        // 触发事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(userId)
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityType(id);
        eventProducer.fireEvent(event);
        return discussPostMapper.updateType(id,type);
    }

    public int setDiscussPostWonderful(int id, int status, int userId){
        // 同步到es中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(userId)
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityType(id);
        eventProducer.fireEvent(event);
        return discussPostMapper.updateStatus(id,status);
    }

    public int deleteDiscussPost(int id, int status, int userId){
        // 触发事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(userId)
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityType(id);
        eventProducer.fireEvent(event);
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
        // 这里要先插入才能获取到id
        int row = discussPostMapper.insertDiscussPost(discussPost);

        // 触发发帖事件，将新发布的帖子存到es服务器中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(userId)
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);
        System.out.println("-----------发帖事件出发成功----------");

        return row;

    }

}
