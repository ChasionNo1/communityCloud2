package com.chasion.controller.interceptor;

import com.chasion.apis.UserFeignApi;
import com.chasion.entity.LoginTicketDTO;
import com.chasion.entity.UserDTO;
import com.chasion.resp.ResultData;
import com.chasion.utils.CookieUtil;
import com.chasion.utils.HostHolder;
import com.chasion.utils.UserUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginTicketInterceptor.class);

    // 在请求开始处理之前，获取ticket

    @Lazy
    @Autowired
    private UserFeignApi userFeignApi;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null){
            // 查询凭证
            ResultData<LoginTicketDTO> resultData = userFeignApi.getTicket(ticket);
            LoginTicketDTO loginTicketDTO = resultData.getData();
//             当前凭证的有效性
            // 这个地方会报错，在一开始启动的时候！
            if (loginTicketDTO != null && loginTicketDTO.getStatus() == 0 && loginTicketDTO.getExpired().after(new Date())){
//                 利用凭证查询user
                UserDTO user = userFeignApi.findUserById(loginTicketDTO.getUserId());
                // 在本次请求中持有用户
                hostHolder.setUser(user);
                // 构建用户的认证结果，并存入SecurityContext，以便于Security进行授权
//                System.out.println("--security-------password-----" + userFeignApi.getPassword(user.getId()).getData());
                Collection<? extends GrantedAuthority> authorities = UserUtils.getAuthorities(user.getType());
//                System.out.println("UserUtils.getAuthorities(user.getType()-->" + authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, userFeignApi.getPassword(user.getId()).getData(), authorities);
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
//                logger.info("SecurityContext已设置: " +authentication);
                // 调试日志
//                System.out.println("=== SecurityContext设置 ===");
//                System.out.println("Principal: " + authentication.getPrincipal());
//                System.out.println("Credentials: " + authentication.getCredentials());
//                System.out.println("Authorities: " + authentication.getAuthorities());
                // 将 SecurityContext 存入 Session
                request.getSession().setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext()
                );
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserDTO user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        SecurityContextHolder.clearContext();
    }
}
