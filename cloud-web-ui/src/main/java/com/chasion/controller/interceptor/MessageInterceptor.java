package com.chasion.controller.interceptor;

import com.chasion.apis.MessageFeignApi;
import com.chasion.entity.UserDTO;
import com.chasion.resp.ResultData;
import com.chasion.utils.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;


@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    @Lazy
    private MessageFeignApi messageFeignApi;


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserDTO user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            HashMap<String, Integer> countMap = messageFeignApi.getConversationCount(user.getId());
            int unreadLetterCount = countMap.get("unreadLetterCount");
            ResultData<HashMap<String, Integer>> noticeData = messageFeignApi.getNoticeData(user.getId(), null);
            int unreadNoticeCount = noticeData.getData().get("unreadNoticeCount");
            modelAndView.addObject("unreadTotalCount", unreadLetterCount + unreadNoticeCount);
        }
    }
}
