package com.chasion.controller;

import com.chasion.apis.UserFeignApi;
import com.chasion.entity.UserDTO;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.utils.CommunityConstant;
import com.chasion.utils.MailClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;


@Controller
@RefreshScope
public class RegisterController implements CommunityConstant {

    /**
     * 负责用户的注册、验证
     * 获取注册页面
     * 响应注册请求
     * 响应激活请求
     *
     * */

    @Autowired
    private UserFeignApi userFeignApi;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${domain}")
    private String domain;

    @Autowired
    private MailClient mailClient;

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
            // 激活邮件 放在这儿进行页面渲染
            UserDTO userDTO = userFeignApi.findUserByUsername(username);
            Context context = new Context();
            context.setVariable("email", userDTO.getEmail());
            context.setVariable("url",domain + "/activation/" + userDTO.getId() + "/" + userDTO.getActivationCode());
            String content = templateEngine.process("/mail/activation", context);
            mailClient.sendMail(userDTO.getEmail(), "激活账号", content);
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

    // 激活链接请求
    // 这里的激活，不应该带这些参数的，userId，太明显了
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        // 通过openfeign调用user服务
        ResultData<Integer> resultData = userFeignApi.activation(userId, code);
        if (resultData.getData() == REGISTER_SUCCESS){
            model.addAttribute("msg", "激活成功，您的账号可以正常使用了!");
            model.addAttribute("target", "/login");

        }else if (resultData.getData() == REGISTER_REPEAT){
            model.addAttribute("msg", "无效操作，该账号已经激活!");
            model.addAttribute("target", "/index");
        }else {
            model.addAttribute("msg", "激活失败，激活码有误!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }
}
