package com.chasion.config;

import com.chasion.utils.CommunityConstant;
import com.chasion.utils.CommunityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig implements CommunityConstant {

    // 静态资源放行配置（替代原 WebSecurity）
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/resources/**");
    }

    // 安全过滤器链配置（替代原 configure(HttpSecurity)）
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable) // 禁用默认登录页
                .httpBasic(AbstractHttpConfigurer::disable); // 禁用HTTP Basic
        // 禁用 CSRF（根据项目需要，生产环境建议开启）
        http.csrf(AbstractHttpConfigurer::disable);
        // 授权配置
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/user/updatePassword",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                ).hasAnyAuthority(AUTHORITY_USER, AUTHORITY_ADMIN, AUTHORITY_MODERATOR)
                .requestMatchers("/discuss/top", "/discuss/wonderful").hasAnyAuthority(AUTHORITY_MODERATOR)
                .requestMatchers("/discuss/delete", "/data/**", "/actuator/**").hasAnyAuthority(AUTHORITY_ADMIN)
                .anyRequest().permitAll()
        );

        // 异常处理配置
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) // 认证入口
                .accessDeniedHandler(new CustomAccessDeniedHandler())           // 授权拒绝处理
        );

        // 退出登录配置（覆盖默认 /logout）
        http.logout(logout -> logout
                .logoutUrl("/security/logout")
                .logoutSuccessUrl("/logout-success") // 自定义退出成功页面
        );

        return http.build();
    }
    // ==================== 自定义处理器 ====================

    // 认证异常处理（未登录）
    private static class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException authException) throws IOException {
            handleResponse(request, response, 403, "你还没有登录！", "/login");
        }
    }

    // 授权异常处理（权限不足）
    private static class CustomAccessDeniedHandler implements AccessDeniedHandler {
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                           org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {
            handleResponse(request, response, 403, "你没有访问此功能的权限！", "/denied");
        }
    }

    // 统一响应处理
    private static void handleResponse(HttpServletRequest request, HttpServletResponse response,
                                       int code, String msg, String redirectUrl) throws IOException {
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // 异步请求返回 JSON
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(code);
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(code, msg));
        } else {
            // 同步请求重定向
            response.sendRedirect(request.getContextPath() + redirectUrl);
        }
    }
}
