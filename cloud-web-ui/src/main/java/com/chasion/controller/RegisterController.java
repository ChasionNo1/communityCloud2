package com.chasion.cloudwebui.controller;

import com.chasion.cloudcommonsapi.apis.UserFeignApi;
import com.chasion.cloudcommonsapi.entity.UserDTO;
import com.chasion.cloudcommonsapi.resp.ResultData;
import com.chasion.cloudcommonsapi.resp.ReturnCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

@Controller
public class RegisterController {

    /**
     * 负责用户的注册、验证
     * 获取注册页面
     * 响应注册请求
     * 响应激活请求
     *
     * */

    @Autowired
    private UserFeignApi userFeignApi;

    // 获取注册页面
    @GetMapping("/register")
    public String getRegisterPage() {
        return "site/register";
    }

    // 响应注册请求
    @PostMapping("/register")
    public String register(Model model, @RequestParam("username") String username,
                           @RequestParam("password")String password,
                           @RequestParam("email") String email) {
        // 这里调用userService的注册方法
        // userService.register(User user)
        // 所以要改成通过openfeign调用
        ResultData<HashMap<String, Object>> resultData = userFeignApi.register(username, password, email);
        if (ReturnCodeEnum.RC200.getCode().equals(resultData.getCode())) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg", resultData.getData().get("usernameMessage"));
            model.addAttribute("passwordMsg", resultData.getData().get("passwordMessage"));
            model.addAttribute("emailMsg", resultData.getData().get("emailMessage"));
            return "/site/register";
        }
    }
}
