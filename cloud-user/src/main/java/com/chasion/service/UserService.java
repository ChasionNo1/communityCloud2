package com.chasion.service;

import com.chasion.dao.LoginTicketMapper;
import com.chasion.entity.LoginTicket;
import com.chasion.entity.UserDTO;
import com.chasion.utils.CommunityConstant;
import com.chasion.utils.CommunityUtil;
import com.chasion.utils.MailClient;
import com.chasion.dao.UserMapper;
import com.chasion.entity.User;
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
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

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
            userDTO.setActivationCode(user.getActivationCode());
        }
        return userDTO;
    }

    // 根据用户名查询用户
    public UserDTO findUserByUsername(String username){
        UserDTO userDTO = new UserDTO();
        User user = userMapper.selectByName(username);
        if(user != null){
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setEmail(user.getEmail());
            userDTO.setType(user.getType());
            userDTO.setStatus(user.getStatus());
            userDTO.setCreateTime(user.getCreateTime());
            userDTO.setHeaderUrl(user.getHeaderUrl());
            userDTO.setActivationCode(user.getActivationCode());
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
        user.setUsername(username);
        user.setEmail(email);
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.Md5(user.getPassword() + user.getSalt()));
        user.setCreateTime(new Date());
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        userMapper.insertUser(user);

        return map;
    }

    // 激活用户
    // 激活
    public int activation(int userId, String activationCode){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return REGISTER_REPEAT;
        }else if(user.getActivationCode().equals(activationCode)){
            userMapper.updateStatus(userId, 1);
//            clearCache(userId);
            return REGISTER_SUCCESS;
        }else {
            return REGISTER_FAILURE;
        }
    }

    // 登录行为
    public Map<String, Object> login(String username, String password, long expired){
        HashMap<String, Object> map = new HashMap<>();
        // 校验
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }else if (StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        // 登录失败
        // 登录成功
        User user = userMapper.selectByName(username);
        // 用户是否存在？
        if (user == null){
            map.put("usernameMsg", "用户不存在");
            return map;
        }else {
            // 用户存在
            // 是否激活
            if (user.getStatus() == 0){
                map.put("usernameMsg", "账号未激活");
                return map;
            }
            // 对比密码，密码是如何设置的？
            //  user.setPassword(CommunityUtil.Md5(user.getPassword() + user.getSalt()));
            // 用户输入的是明文密码
            String inputPassword = CommunityUtil.Md5(password + user.getSalt());
            String savePassword = user.getPassword();
            System.out.println("inputPassword:"+inputPassword);
            System.out.println("savePassword:"+savePassword);
            if (!savePassword.equals(inputPassword)){
                // 密码不正确
                map.put("passwordMsg", "密码不正确");
                return map;
            }else {
                map.put("passwordMsg", "密码正确");
                // 登录成功，设置登录凭证
                LoginTicket loginTicket = new LoginTicket();
                loginTicket.setUserId(user.getId());
                // 登录成功状态为0
                loginTicket.setStatus(0);
                loginTicket.setTicket(CommunityUtil.generateUUID());
                loginTicket.setExpired(new Date(System.currentTimeMillis() + expired));
                // 保存凭证到redis里
//                String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
//                redisTemplate.opsForValue().set(ticketKey, loginTicket);
                loginTicketMapper.insertLoginTicket(loginTicket);
                // 给客户发送ticket
                map.put("ticket", loginTicket.getTicket());
            }
        }
        return map;
    }

    // 退出登录
    public void logout(String ticket){
        // 在redis里的
//        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
//        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
//        loginTicket.setStatus(1);
//        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        // 在mysql里的，更新状态就完事了
        loginTicketMapper.updateLoginTicket(ticket, 1);
    }

    // 获取用户的登录凭证
    public LoginTicket getLoginTicket(String ticket){
//        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
//        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicketMapper.selectLoginTicket(ticket);
    }
}
