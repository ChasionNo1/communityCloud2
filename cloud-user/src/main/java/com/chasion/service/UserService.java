package com.chasion.clouduser.service;

import com.chasion.cloudcommonsapi.entity.UserDTO;
import com.chasion.cloudcommonsapi.utils.CommunityUtil;
import com.chasion.cloudcommonsapi.utils.MailClient;
import com.chasion.clouduser.dao.UserMapper;
import com.chasion.clouduser.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Value("${domain}")
    private String domain;

    // 根据用户id查询用户名
    public UserDTO findUserById(int id){
        UserDTO userDTO = new UserDTO();
        User user = userMapper.selectById(id);
        if(user != null){
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setEmail(user.getEmail());
            userDTO.setType(user.getType());
            userDTO.setStatus(user.getStatus());
            userDTO.setCreateTime(user.getCreateTime());
            userDTO.setHeaderUrl(user.getHeaderUrl());
        }
        return userDTO;
    }

    // 注册用户
    public HashMap<String,Object> register(String username, String password, String email) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        // 参数校验
        if (username == null || password == null || email == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(username)){
            map.put("usernameMessage", "用户名不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMessage", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(email)){
            map.put("emailMessage", "邮箱不能为空!");
            return map;
        }
        // 用户是否已注册
        User u = userMapper.selectByName(username);
        if (u != null){
            map.put("usernameMessage", "该账号已存在!");
            return map;
        }
        u = userMapper.selectByEmail(email);
        if (u != null){
            map.put("emailMessage","该邮箱已被存在!");
            return map;
        }
        // 注册用户
        User user = new User();
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.Md5(user.getPassword() + user.getSalt()));
        user.setCreateTime(new Date());
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("url",domain + "/activation/" + user.getId() + "/" + user.getActivationCode());
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }
}
