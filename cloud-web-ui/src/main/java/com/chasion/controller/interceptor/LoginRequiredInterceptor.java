package com.chasion.controller.interceptor;

import com.chasion.annotation.LoginRequired;
import com.chasion.entity.UserDTO;
import com.chasion.utils.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserDTO user = hostHolder.getUser();
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            /// A.isAnnotationPresent(B.class);
            // 大白话：B类型的注解是否在A类上。
            if (method.isAnnotationPresent(LoginRequired.class ) && user == null) {
                // 是这种类型的注解，且用户值为空，即：需要登录但没登录，进行拦截
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
