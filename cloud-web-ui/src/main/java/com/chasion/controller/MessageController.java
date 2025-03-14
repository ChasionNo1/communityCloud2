package com.chasion.controller;

import com.chasion.apis.MessageFeignApi;
import com.chasion.apis.UserFeignApi;
import com.chasion.entity.MessageDTO;
import com.chasion.entity.Page;
import com.chasion.entity.UserDTO;
import com.chasion.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageFeignApi messageFeignApi;

    @Autowired
    private UserFeignApi userFeignApi;


    // 私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        // 分页信息
        page.setLimit(5);
        UserDTO user = hostHolder.getUser();
        // 调用一次服务把所有数据都要过来，跨服务多次请求会消耗资源
        int conversationCount = messageFeignApi.getConversationCount(user.getId());
        page.setRows(conversationCount);
        page.setPath("/letter/list");
        // 会话列表
        List<MessageDTO> conversations = messageFeignApi.getConversation(user.getId(), page.getOffset(), page.getLimit());
        // 未读私信所有
        int unreadLetterCount = messageFeignApi.getUnreadLetterCount(user.getId(), null);
        List<Map<String, Object>> conversationVoList = new ArrayList<>();
        // 每个会话的未读消息何会话条数

        for (MessageDTO message :
                conversations) {
            HashMap<String, Object> map = new HashMap<>();
            // 获取会话的未读消息数
            int conversationUnreadLetterCount = messageService.getUnreadLetterCount(user.getId(), message.getConversationId());
            // 获取会话的条数
            int letterCount = messageService.getLetterCount(message.getConversationId());
            // 发送者的头像和名称等信息，如果是登录用户给朋友发送的私信，显示朋友头像，如果是朋友给登录用户发送的私信，显示朋友头像
            int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
            map.put("message", message);
            map.put("culc", conversationUnreadLetterCount);
            map.put("letterCount", letterCount);
            map.put("target", userFeignApi.findUserById(targetId));
            conversationVoList.add(map);
        }

        model.addAttribute("conversationVoList", conversationVoList);
        model.addAttribute("unreadLetterCount", unreadLetterCount);
        int unreadNoticeTotalCount = messageService.getUnreadNoticeCount(user.getId(), null);
        model.addAttribute("unreadNoticeTotalCount", unreadNoticeTotalCount);


        return "/site/letter";
    }
}
