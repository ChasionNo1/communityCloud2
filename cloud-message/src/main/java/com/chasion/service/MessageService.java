package com.chasion.service;

import com.chasion.dao.MessageMapper;
import com.chasion.entity.Message;
import com.chasion.entity.MessageDTO;
import com.chasion.utils.HostHolder;
import com.chasion.utils.SensitiveFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private HostHolder hostHolder;

    // 获取的是对话内容
    public List<MessageDTO> getConversation(int userId, int offset, int limit){
        List<Message> conversations = messageMapper.getConversations(userId, offset, limit);
        List<MessageDTO> dtos = new ArrayList<>();
        for (Message conversation : conversations) {
            MessageDTO dto = new MessageDTO();
            BeanUtils.copyProperties(conversation, dto);
            dtos.add(dto);
        }
        return dtos;
    }

    public int getConversationCount(int userId){
        return messageMapper.getConversationCount(userId);
    }

    // 获取私信列表
    public List<MessageDTO> getLetters(String conversationId, int offset, int limit){
        List<Message> letters = messageMapper.getLetters(conversationId, offset, limit);
        List<MessageDTO> dtos = new ArrayList<>();
        for (Message letter : letters) {
            MessageDTO dto = new MessageDTO();
            BeanUtils.copyProperties(letter, dto);
            dtos.add(dto);
        }
        return dtos;
    }

    public int getLetterCount(String conversationId){
        return messageMapper.getLetterCount(conversationId);
    }

    public int getUnreadLetterCount(int userId, String conversationId){
        return messageMapper.getUnreadLetterCount(userId, conversationId);
    }

    public int addMessage(Message message){
        // 过滤敏感词
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Message> messages){
        // 获取未读letter的id
        ArrayList<Integer> ids = new ArrayList<>();
        if (messages != null && !messages.isEmpty()){
            for (Message message :
                    messages) {
                // 如果当前登录的用户是接收者，且消息未读
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        if (!ids.isEmpty()){
            return messageMapper.updateStatus(ids, 1);
        }else {
            return 1;
        }
    }

//    public List<Integer> getUnreadList(List<Message> messages){
//        // 获取未读letter的id
//        ArrayList<Integer> ids = new ArrayList<>();
//        if (messages != null && !messages.isEmpty()){
//            for (Message message :
//                    messages) {
//                // 如果当前登录的用户是接收者，且消息未读
//                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
//                    ids.add(message.getId());
//                }
//            }
//        }
//        return ids;
//    }

    public Message getLastNotice(int userId, String topic){
        return messageMapper.getLatestNotice(userId, topic);
    }

    public int getNoticeCount(int userId, String topic){
        return messageMapper.getNoticeCount(userId, topic);
    }

    public int getUnreadNoticeCount(int userId, String topic){
        return messageMapper.getUnreadNoticeCount(userId, topic);
    }

    public List<Message> getNotices(int userId, String topic, int offset, int limit){
        return messageMapper.getNotices(userId, topic, offset, limit);
    }
}
