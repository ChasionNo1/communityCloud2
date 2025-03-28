package com.chasion.apis;

import com.chasion.entity.MessageDTO;
import com.chasion.resp.ResultData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

@FeignClient(value = "cloud-message", path = "/messageService", fallback = MessageFeignApiFallback.class)
public interface MessageFeignApi {

    @GetMapping("/get/getConversationCount")
    public HashMap<String, Integer> getConversationCount(@RequestParam("userId") int userId);

    @GetMapping("/get/getConversation")
    public List<MessageDTO> getConversation(@RequestParam("userId") int userId,
                                            @RequestParam("offset") int offset,
                                            @RequestParam("limit") int limit);

    @PostMapping("/add/message")
    public int addMessage(@RequestBody MessageDTO message);

    @GetMapping("/get/letters")
    public List<MessageDTO> getLetters(@RequestParam("conversationId") String conversationId, @RequestParam("offset") int offset, @RequestParam("limit") int limit);

    @PostMapping("/read/letters")
    public int readMessages(@RequestBody List<MessageDTO> messages,  @RequestParam("userId") int userId);

    @GetMapping("/get/letterCount")
    public int getLetterCount(@RequestParam("conversationId") String conversationId);

    @PostMapping("/delete/letter")
    public int deleteMessage(@RequestParam("id") int id);

    @GetMapping("/get/lastNotice")
    public ResultData<MessageDTO> getLastNotice(@RequestParam("userId") int userId, @RequestParam("topic") String topic);

    @GetMapping("/get/notice/data")
    public ResultData<HashMap<String, Integer>> getNoticeData(@RequestParam("userId") int userId, @RequestParam(value = "topic", required = false) String topic);

    @GetMapping("/get/noticeList")
    public ResultData<List<MessageDTO>> getNoticeList(@RequestParam("userId") int userId,
                                                      @RequestParam("topic") String topic,
                                                      @RequestParam("offset") int offset,
                                                      @RequestParam("limit") int limit);

    @PostMapping("/set/notice/read")
    public ResultData<String> readMessage(@RequestBody List<MessageDTO> messageDTOS, @RequestParam("userId") int userId);
}

