package com.chasion.controller.advice;

import com.alibaba.fastjson2.JSON;
import com.chasion.exc.BaseException;
import com.chasion.resp.ResultData;
import feign.FeignException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(annotations = Controller.class)
public class WebExceptionHandler {
//     处理业务异常（如用户不存在）
    @ExceptionHandler(BaseException.class)
    public String handleBusinessException(BaseException e, Model model) {
        model.addAttribute("errorCode", e.getCode());
        model.addAttribute("errorMsg", e.getMessage());
        return "error/error"; // 渲染 error.html
    }

    // 处理 Feign 调用异常（如服务不可用）
    @ExceptionHandler(FeignException.class)
    public String handleFeignException(FeignException e, Model model) {
        // 解析 Feign 异常中的错误信息
        String errorBody = e.contentUTF8();
        ResultData<?> response = JSON.parseObject(errorBody, ResultData.class);
        model.addAttribute("errorCode", response.getCode());
        model.addAttribute("errorMsg", response.getMessage());
        return "error/service-error"; // 渲染服务调用错误页
    }

//    // 处理凭证过期异常（跳转登录页）
//    @ExceptionHandler(TokenExpiredException.class)
//    public String handleTokenExpiredException() {
//        return "redirect:/login?error=token_expired";
//    }

    // 兜底异常处理
    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Model model) {
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorMsg", "系统繁忙，请稍后重试");
        return "/error/500";
    }
}
