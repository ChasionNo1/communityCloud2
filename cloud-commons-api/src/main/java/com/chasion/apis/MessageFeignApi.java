package com.chasion.apis;

import com.chasion.entity.MessageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

@FeignClient(value = "cloud-message", path = "/messageService")
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
}

