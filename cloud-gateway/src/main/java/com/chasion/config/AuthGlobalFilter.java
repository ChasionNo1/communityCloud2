package com.chasion.config;

import com.chasion.apis.UserFeignApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * 自定义全局过滤器：
 * 网关层实验凭证校验
 *
 * */
//@Component
//public class AuthGlobalFilter implements GlobalFilter, Ordered {
//
//    // 跨服务调用，后续可以使用redis来优化
//    @Autowired
//    private UserFeignApi userFeignApi;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        // 获取请求
//        HttpServletRequest request = (HttpServletRequest) exchange.getRequest();
//        // 从cookie里获取ticket，这样的警告或错误通常意味着你在非阻塞环境中调用了阻塞操作。
//        // 这会导致当前线程或执行上下文被阻塞，而其他需要执行的任务可能因此而无法及时得到处理，
//        // 从而导致“线程饥饿”（thread starvation）或性能问题。
//        // 所以这样做的意义不大，还是放到下游来做！
//        String ticket = userFeignApi.verifyTicket(request);
//        return null;
//    }
//
//    @Override
//    public int getOrder() {
//        return 0;
//    }
//}
