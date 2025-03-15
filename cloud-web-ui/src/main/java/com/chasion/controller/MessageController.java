package com.chasion.controller;

import com.alibaba.fastjson2.JSON;
import com.chasion.apis.MessageFeignApi;
import com.chasion.apis.UserFeignApi;
import com.chasion.entity.MessageDTO;
import com.chasion.entity.Page;
import com.chasion.entity.UserDTO;
import com.chasion.utils.CommunityUtil;
import com.chasion.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
        if(user == null){
            throw new RuntimeException("user is null");
        }
        // 调用一次服务把所有数据都要过来，跨服务多次请求会消耗资源
        HashMap<String, Integer> countMap = messageFeignApi.getConversationCount(user.getId());
        page.setRows(countMap.get("conversationCount"));
        page.setPath("/letter/list");
        // 会话列表
        // 这里还获取了会话列表，可以在这次请求里完成
        System.out.println("userID" + user.getId());
        List<MessageDTO> conversationVoList = messageFeignApi.getConversation(user.getId(), page.getOffset(), page.getLimit());
        // 未读私信所有
        int unreadLetterCount = countMap.get("unreadLetterCount");
        // 每个会话的未读消息何会话条数
        for (MessageDTO conversationVo :
                conversationVoList) {
//            String s = JSON.toJSONString(conversationVo.get("message"));
//            MessageDTO message = JSON.parseObject(s, MessageDTO.class);
            // 获取会话的未读消息数
            // 这里循环读取速度肯定慢，所以要一次请求完成
//            int conversationUnreadLetterCount = messageService.getUnreadLetterCount(user.getId(), message.getConversationId());
//            // 获取会话的条数
//            int letterCount = messageService.getLetterCount(message.getConversationId());

            // 发送者的头像和名称等信息，如果是登录用户给朋友发送的私信，显示朋友头像，如果是朋友给登录用户发送的私信，显示朋友头像
            int targetId = user.getId() == conversationVo.getFromId() ? conversationVo.getToId() : conversationVo.getFromId();
            conversationVo.setTarget(userFeignApi.findUserById(targetId));
        }

        model.addAttribute("conversationVoList", conversationVoList);
        model.addAttribute("unreadLetterCount", unreadLetterCount);
        int unreadNoticeTotalCount = countMap.get("unreadNoticeCount");
        model.addAttribute("unreadNoticeTotalCount", unreadNoticeTotalCount);


        return "/site/letter";
    }

    // 发送私信功能
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        // 对方目标
        UserDTO target = userFeignApi.findUserByUsername(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "用户不存在！");
        }

        // 构造消息
        MessageDTO message = new MessageDTO();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setContent(content);
        String conversationId = message.getFromId() < message.getToId() ? message.getFromId() + "_" + message.getToId() : message.getToId() + "_" + message.getFromId();
        message.setConversationId(conversationId);
        message.setCreateTime(new Date());
        message.setStatus(0);
        messageFeignApi.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    // 私信详情页面
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(Model model, Page page, @PathVariable("conversationId") String conversationId) {
        //设置分页信息
        page.setPath("/letter/detail/" + conversationId);
        page.setLimit(5);
        page.setRows(messageFeignApi.getLetterCount(conversationId));

        // 对话内容的封装
        String[] ids = conversationId.split("_");
        int targetId = hostHolder.getUser().getId() == Integer.parseInt(ids[0]) ? Integer.parseInt(ids[1]) : Integer.parseInt(ids[0]);
        model.addAttribute("target", userFeignApi.findUserById(targetId));
        // 这里也是获取list和设置为已读，两次调用了
        List<MessageDTO> letters = messageFeignApi.getLetters(conversationId, page.getOffset(), page.getLimit());
        messageFeignApi.readMessages(letters, hostHolder.getUser().getId());
        List<Map<String, Object>> letterVoList = new ArrayList<>();
        for (MessageDTO letter :
                letters) {
            HashMap<String, Object> map = new HashMap<>();
            // 放内容，放发送者的信息
            map.put("letter", letter);
            // 这里又有循环的调用
            UserDTO user = userFeignApi.findUserById(letter.getFromId());
            map.put("user", user);
            letterVoList.add(map);
        }

        model.addAttribute("letters", letterVoList);
        return "/site/letter-detail";
    }

    // 删除私信
    @PostMapping("/letter/delete")
    public String deleteLetter(int id) {
        messageFeignApi.deleteMessage(id);
        return CommunityUtil.getJSONString(0);
    }




}
