package com.chasion.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.chasion.apis.MessageFeignApi;
import com.chasion.apis.UserFeignApi;
import com.chasion.entity.MessageDTO;
import com.chasion.entity.Page;
import com.chasion.entity.UserDTO;
import com.chasion.resp.ResultData;
import com.chasion.utils.CommunityUtil;
import com.chasion.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

import static com.chasion.utils.CommunityConstant.*;

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


    // 响应系统通知
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        UserDTO user = hostHolder.getUser();
        // 查询三种类型通知的数量，时间，未读消息数量，以及target
        MessageDTO lastComment = messageFeignApi.getLastNotice(user.getId(), TOPIC_COMMENT).getData();
        HashMap<String, Object> commentVO = new HashMap<>();
        commentVO.put("lastComment", lastComment);
        if (lastComment != null){

            String content = HtmlUtils.htmlUnescape(lastComment.getContent());
            HashMap data = JSONObject.parseObject(content, HashMap.class);
            commentVO.put("user", userFeignApi.findUserById((Integer)data.get("userId")));
            commentVO.put("entityType", data.get("entityType"));
            commentVO.put("entityId", data.get("entityId"));
            commentVO.put("postId", data.get("postId"));
            Date createTime = lastComment.getCreateTime();
            ResultData<HashMap<String, Integer>> noticeData = messageFeignApi.getNoticeData(user.getId(), TOPIC_COMMENT);
            int commentCount = noticeData.getData().get("noticeCount");
            int unreadCommentCount = noticeData.getData().get("unreadNoticeCount");
            commentVO.put("commentCount", commentCount);
            commentVO.put("unreadCommentCount", unreadCommentCount);
            commentVO.put("createTime", createTime);
        }
        model.addAttribute("commentVO", commentVO);

        MessageDTO lastLike = messageFeignApi.getLastNotice(user.getId(), TOPIC_LIKE).getData();
        HashMap<String, Object> LikeVO = new HashMap<>();
        LikeVO.put("lastLike", lastLike);
        if (lastLike != null){
            String content = HtmlUtils.htmlUnescape(lastLike.getContent());
            HashMap data = JSONObject.parseObject(content, HashMap.class);
            LikeVO.put("user", userFeignApi.findUserById((Integer)data.get("userId")));
            LikeVO.put("entityType", data.get("entityType"));
            LikeVO.put("entityId", data.get("entityId"));
            LikeVO.put("postId", data.get("postId"));
            Date createTime = lastLike.getCreateTime();
            ResultData<HashMap<String, Integer>> noticeData = messageFeignApi.getNoticeData(user.getId(), TOPIC_LIKE);
            int likeCount = noticeData.getData().get("noticeCount");
            int unreadLikeCount = noticeData.getData().get("unreadNoticeCount");
            LikeVO.put("likeCount", likeCount);
            LikeVO.put("unreadLikeCount", unreadLikeCount);
            LikeVO.put("createTime", createTime);
        }
        model.addAttribute("likeVO", LikeVO);

        MessageDTO lastFollow = messageFeignApi.getLastNotice(user.getId(), TOPIC_FOLLOW).getData();
        HashMap<String, Object> FollowVO = new HashMap<>();
        if (lastFollow != null){
            FollowVO.put("lastFollow", lastFollow);
            String content = HtmlUtils.htmlUnescape(lastFollow.getContent());
            HashMap data = JSONObject.parseObject(content, HashMap.class);
            FollowVO.put("user", userFeignApi.findUserById((Integer)data.get("userId")));
            FollowVO.put("entityType", data.get("entityType"));
            FollowVO.put("entityId", data.get("entityId"));
            Date createTime = lastFollow.getCreateTime();
            ResultData<HashMap<String, Integer>> noticeData = messageFeignApi.getNoticeData(user.getId(), TOPIC_FOLLOW);
            int followCount = noticeData.getData().get("noticeCount");
            int unreadFollowCount = noticeData.getData().get("unreadNoticeCount");
            FollowVO.put("followCount", followCount);
            FollowVO.put("unreadFollowCount", unreadFollowCount);
            FollowVO.put("createTime", createTime);
        }
        model.addAttribute("followVO", FollowVO);
        // 查询所有未读的消息
        ResultData<HashMap<String, Integer>> noticeData = messageFeignApi.getNoticeData(user.getId(), null);
//        int unreadNoticeTotalCount = messageService.getUnreadNoticeCount(user.getId(), null);
        model.addAttribute("unreadNoticeTotalCount", noticeData.getData().get("unreadNoticeCount"));
        // 查询未读的私信
        HashMap<String, Integer> countMap = messageFeignApi.getConversationCount(user.getId());
        int unreadLetterCount = countMap.get("unreadLetterCount");
        model.addAttribute("unreadLetterCount", unreadLetterCount);
        return "/site/notice";
    }

    // 处理三种不同类型的通知
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        UserDTO user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageFeignApi.getNoticeData(user.getId(), topic).getData().get("noticeCount"));
        // 获取通知列表
        List<MessageDTO> noticeList = messageFeignApi.getNoticeList(user.getId(), topic, page.getOffset(), page.getLimit()).getData();
        // 封装VO
        List<Map<String, Object>> noticeVO = new ArrayList<>();
        if (noticeList != null){
            for (MessageDTO message : noticeList) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("notice", message);
                String content = HtmlUtils.htmlUnescape(message.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userFeignApi.findUserById((Integer)data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知的作者
                map.put("fromUser", userFeignApi.findUserById(message.getFromId()));
                noticeVO.add(map);
            }
        }
        model.addAttribute("noticeVO", noticeVO);

        // 设置已读，调用service处理
        messageFeignApi.readMessage(noticeList, user.getId());

        return "/site/notice-detail";
    }





}
