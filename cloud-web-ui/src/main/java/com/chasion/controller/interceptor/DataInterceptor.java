package com.chasion.controller.interceptor;

import com.chasion.entity.UserDTO;
import com.chasion.service.DataService;
import com.chasion.utils.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计uv
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);
        // 统计dau
        UserDTO user = hostHolder.getUser();
        System.out.println("data interceptor user: " + user);
        if (user != null){
            dataService.recordDau(user.getId());
        }

        return true;
    }
}
