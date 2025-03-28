package com.chasion.controller;

import com.chasion.entity.Message;
import com.chasion.entity.MessageDTO;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/messageService")
@RefreshScope
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
    public HashMap<String, Integer> getConversationCount(@RequestParam("userId") int userId){
        int conversationCount = messageService.getConversationCount(userId);
        int unreadLetterCount = messageService.getUnreadLetterCount(userId, null);
        int unreadNoticeCount = messageService.getUnreadNoticeCount(userId, null);
        HashMap<String, Integer> map = new HashMap<>();
        map.put("conversationCount", conversationCount);
        map.put("unreadLetterCount", unreadLetterCount);
        map.put("unreadNoticeCount", unreadNoticeCount);
        return map;
    }

    // 获取会话列表
    // user.getId(), page.getOffset(), page.getLimit()
    // 顺带把计数数据也获取了
    @GetMapping("/get/getConversation")
    public List<MessageDTO> getConversation(@RequestParam("userId") int userId,
                                            @RequestParam("offset") int offset,
                                            @RequestParam("limit") int limit){
        return messageService.getConversation(userId, offset, limit);
    }


    // 添加私信
    @PostMapping("/add/message")
    public int addMessage(@RequestBody MessageDTO message){
        return messageService.addMessage(message);
    }

    // 获取当前对话的私信内容，
    // 这两个操作还不能合并在一起，一个是get请求，一个是post请求
    @GetMapping("/get/letters")
    public List<MessageDTO> getLetters(@RequestParam("conversationId") String conversationId, @RequestParam("offset") int offset, @RequestParam("limit") int limit){
        return messageService.getLetters(conversationId, offset, limit);
    }


    //  并设置为已读
    @PostMapping("/read/letters")
    public int readMessages(@RequestBody List<MessageDTO> messages, @RequestParam("userId") int userId){
        return messageService.readMessage(messages, userId);
    }

    @GetMapping("/get/letterCount")
    public int getLetterCount(@RequestParam("conversationId") String conversationId){
        return messageService.getLetterCount(conversationId);
    }

    // 删除私信
    @PostMapping("/delete/letter")
    public int deleteMessage(@RequestParam("id") int id){
        return messageService.deleteMessage(id);
    }

    // 获取最近的通知内容
    // MessageDTO lastComment = messageService.getLastNotice(user.getId(), TOPIC_COMMENT);
    @GetMapping("/get/lastNotice")
    public ResultData<MessageDTO> getLastNotice(@RequestParam("userId") int userId, @RequestParam(value = "topic") String topic){
        MessageDTO lastNotice = messageService.getLastNotice(userId, topic);
        System.out.println("lastNotice: " + lastNotice);
        ResultData<MessageDTO> resultData = new ResultData<>();
        if (lastNotice != null){
            resultData.setCode(ReturnCodeEnum.RC200.getCode());
            resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
            resultData.setData(lastNotice);
        }else {
            resultData.setCode(ReturnCodeEnum.RC999.getCode());
            resultData.setMessage(ReturnCodeEnum.RC999.getMessage());
        }
        return resultData;

    }


    // 获取系统消息相关的数据
    // int commentCount = messageService.getNoticeCount(user.getId(), TOPIC_COMMENT);
    // int unreadCommentCount = messageService.getUnreadNoticeCount(user.getId(), TOPIC_COMMENT);
    @GetMapping("/get/notice/data")
    public ResultData<HashMap<String, Integer>> getNoticeData(@RequestParam("userId") int userId, @RequestParam(value = "topic", required = false) String topic){
        ResultData<HashMap<String, Integer>> resultData = new ResultData<>();
        int noticeCount = messageService.getNoticeCount(userId, topic);
        int unreadNoticeCount = messageService.getUnreadNoticeCount(userId, topic);
        resultData.setCode(ReturnCodeEnum.RC200.getCode());
        resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
        HashMap<String, Integer> map = new HashMap<>();
        map.put("noticeCount", noticeCount);
        map.put("unreadNoticeCount", unreadNoticeCount);
        resultData.setData(map);
        return resultData;
    }

    // 获取系统通知消息列表
    // messageFeignApi.getNotices(user.getId(), topic, page.getOffset(), page.getLimit());
    @GetMapping("/get/noticeList")
    public ResultData<List<MessageDTO>> getNoticeList(@RequestParam("userId") int userId,
                                                      @RequestParam("topic") String topic,
                                                      @RequestParam("offset") int offset,
                                                      @RequestParam("limit") int limit){
        List<MessageDTO> notices = messageService.getNotices(userId, topic, offset, limit);
        ResultData<List<MessageDTO>> resultData = new ResultData<>();
        if (notices != null){
            resultData.setCode(ReturnCodeEnum.RC200.getCode());
            resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
            resultData.setData(notices);
        }else {
            resultData.setCode(ReturnCodeEnum.RC999.getCode());
            resultData.setMessage(ReturnCodeEnum.RC999.getMessage());
        }
        return resultData;
    }

    // 设置系统消息为已读
    @PostMapping("/set/notice/read")
    public ResultData<String> readMessage(@RequestBody List<MessageDTO> messageDTOS, @RequestParam("userId") int userId){
        int rows = messageService.readMessage(messageDTOS, userId);
        ResultData<String> resultData = new ResultData<>();
        if (rows > 0){
            resultData.setCode(ReturnCodeEnum.RC200.getCode());
            resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
        }else {
            resultData.setCode(ReturnCodeEnum.RC999.getCode());
            resultData.setMessage(ReturnCodeEnum.RC999.getMessage());
        }
        return resultData;
    }








}
