package com.chasion.controller;

import com.chasion.entity.MessageDTO;
import com.chasion.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/messageService")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 这里把能获取到的数据，一次性获取，避免调用服务
     * getConversationCount(userId):获取对话数量
     * getUnreadLetterCount(userId,null):获取未读取私信的数量
     * getUnreadNoticeCount(userId,null):获取未读消息的数量
     */
    @GetMapping("/get/getConversationCount")
    public int getConversationCount(int userId){
        return messageService.getConversationCount(userId);
    }

    // 获取会话列表
    // user.getId(), page.getOffset(), page.getLimit()
    @GetMapping("/get/getConversation")
    public List<MessageDTO> getConversation(@RequestParam("userId") int userId,
                                            @RequestParam("offset") int offset,
                                            @RequestParam("limit") int limit){
        return messageService.getConversation(userId, offset, limit);
    }






}
