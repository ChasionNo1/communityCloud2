package com.chasion.controller;

import com.chasion.apis.UserFeignApi;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.utils.CommunityConstant;
import com.chasion.utils.CommunityUtil;
import com.chasion.utils.MailClient;
import com.chasion.utils.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Controller
@RefreshScope
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private Producer captchaProducer;

    @Autowired
    private UserFeignApi userFeignApi;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;


    // 响应登录页面
    @GetMapping("/login")
    public String login() {
        return "site/login";
    }

    // 忘记密码

    // 响应登录请求
    // 处理登录请求
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(Model model, String username, String password, String code, boolean rememberMe,
                        HttpSession session, HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 验证码在客户获取登录页面的时候加载，信息已经存入到session中，
        // 此时需要将客户从前端页面中输入的验证码和session中取到的验证码进行对比
//        String serverKaptcha = (String)session.getAttribute("captcha");
        // 重构：从redis里取验证码
        String serverKaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            serverKaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }


        // 验证码不正确，页面需要回填数据，重新填写验证码即可
        if (StringUtils.isBlank(serverKaptcha) || StringUtils.isBlank(code) || !serverKaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg", "验证码不正确");
            return "site/login";
        }

        // 账号，密码验证
        int expired = rememberMe ? CommunityConstant.REMEMBER_EXPIRATION_TIME : CommunityConstant.DEFAULT_EXPIRATION_TIME;
        // 修改：调用userFeignApi实现登录

        ResultData<Map<String, Object>> login = userFeignApi.login(username, password, expired);
        Map<String, Object> map = login.getData();
        if (map.containsKey("ticket")){
            // 登录 成功
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath("/");
            cookie.setMaxAge(expired);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }

    }


    // 响应验证码请求
    // 获取验证码
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = captchaProducer.createText();
        BufferedImage image = captchaProducer.createImage(text);

//         将验证码存入session
//        session.setAttribute("captcha", text);
        // 重构验证码存放的位置，放到redis里
        // 需要临时凭证
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath("/");
        response.addCookie(cookie);
        // 将验证码存入redis中
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        }catch (IOException e){
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    // 退出登录
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userFeignApi.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    // 忘记密码
    @GetMapping("/user/forget")
    public String getForgetPage(Model model) {
        return "site/forget";
    }

    // 忘记密码-发送验证码
    @PostMapping("/send/verifyCode")
    @ResponseBody
    public ResultData<String> forgetPassword(HttpSession session, @RequestParam("email") String email) {
        // 开始响应通过邮件发送验证码
        // 先查询这个邮件是否是存在用户，用户状态如何
        ResultData<String> resultData = userFeignApi.checkEmail(email);
        if (Objects.equals(resultData.getCode(), ReturnCodeEnum.RC200.getCode())){
            // 如果验证成功就发送验证码
            // 调用邮件发送功能：发送验证码
            // 随机生成验证码
            SecureRandom random = new SecureRandom();
            int code = random.nextInt(900000) + 100000;
            // 验证码还要放在session里
            session.setAttribute("code", String.valueOf(code));
            Context context = new Context();
            context.setVariable("email", email);
            context.setVariable("code", String.valueOf(code));
            String content = templateEngine.process("/mail/forget", context);
            mailClient.sendMail(email, "验证码", content);
        }else {
            resultData.setCode(ReturnCodeEnum.RC500.getCode());
            resultData.setMessage(ReturnCodeEnum.RC500.getMessage());
        }
        return resultData;
    }

    // 响应忘记密码表单
    @PostMapping("/password/forget")
    public String forgetPassword(String email, String verifyCode, String newPassword, HttpSession session, Model model) {
        // 接收表单数据，修改密码
        String code = (String) session.getAttribute("code");
        // 如果验证码不一致
        if (!code.equals(verifyCode)){
            model.addAttribute("verifyCodeMsg", "验证码有误!");
            return "site/forget";
        }else {
            // 验证码一致，调用user服务
            ResultData<String> resultData = userFeignApi.forgetPassword(email, newPassword);
            if (Objects.equals(resultData.getCode(), ReturnCodeEnum.RC200.getCode())){
                // 直接跳转到登录页面
                return "redirect:/login";
            }else {
                // 如果失败了，这里没有对密码进行校验，只是针对邮箱
                model.addAttribute("emailMsg", "邮箱有误!");
                // 继续在这个页面
                return "site/forget";
            }
        }
    }
}
