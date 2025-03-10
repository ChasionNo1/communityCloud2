package com.chasion.controller;

import com.chasion.utils.CommunityConstant;
import com.chasion.utils.CommunityUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private Producer captchaProducer;


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
                        javax.servlet.http.HttpSession session, javax.servlet.http.HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) {
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
            return "/site/login";
        }

        // 账号，密码验证
        int expired = rememberMe ? CommunityConstant.REMEMBER_EXPIRATION_TIME : CommunityConstant.DEFAULT_EXPIRATION_TIME;
        Map<String, Object> map = userService.login(username, password, expired);
        if (map.containsKey("ticket")){
            // 登录 成功
            javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("ticket", map.get("ticket").toString());
            cookie.setPath("/");
            cookie.setMaxAge(expired);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
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
        session.setAttribute("captcha", text);
        // 重构验证码存放的位置，放到redis里
        // 需要临时凭证
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath("/");
        response.addCookie(cookie);
        // 将验证码存入redis中
//        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
//        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        }catch (IOException e){
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }
}
