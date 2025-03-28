package com.chasion.apis;

import com.chasion.entity.MessageDTO;
import com.chasion.resp.ResultData;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class MessageFeignApiFallback implements MessageFeignApi {

    @Override
    public HashMap<String, Integer> getConversationCount(int userId) {
        return null;
    }

    @Override
    public List<MessageDTO> getConversation(int userId, int offset, int limit) {
        return List.of();
    }

    @Override
    public int addMessage(MessageDTO message) {
        return 0;
    }

    @Override
    public List<MessageDTO> getLetters(String conversationId, int offset, int limit) {
        return List.of();
    }

    @Override
    public int readMessages(List<MessageDTO> messages, int userId) {
        return 0;
    }

    @Override
    public int getLetterCount(String conversationId) {
        return 0;
    }

    @Override
    public int deleteMessage(int id) {
        return 0;
    }

    @Override
    public ResultData<MessageDTO> getLastNotice(int userId, String topic) {
        return null;
    }

    @Override
    public ResultData<HashMap<String, Integer>> getNoticeData(int userId, String topic) {
        return null;
    }

    @Override
    public ResultData<List<MessageDTO>> getNoticeList(int userId, String topic, int offset, int limit) {
        return null;
    }

    @Override
    public ResultData<String> readMessage(List<MessageDTO> messageDTOS, int userId) {
        return null;
    }
}
