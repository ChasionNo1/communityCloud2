package com.chasion.apis;

import com.chasion.entity.MessageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "cloud-message", path = "/messageService")
public interface MessageFeignApi {

    @GetMapping("/get/getConversationCount")
    public int (int userId);

    @GetMapping("/get/getConversation")
    public List<MessageDTO> getConversation(@RequestParam("userId") int userId,
                                            @RequestParam("offset") int offset,
                                            @RequestParam("limit") int limit);
}
