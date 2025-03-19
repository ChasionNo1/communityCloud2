package com.chasion.service;

import com.chasion.dao.LoginTicketMapper;
import com.chasion.entity.LoginTicket;
import com.chasion.entity.LoginTicketDTO;
import com.chasion.entity.UserDTO;
import com.chasion.utils.CommunityConstant;
import com.chasion.utils.CommunityUtil;
import com.chasion.utils.MailClient;
import com.chasion.dao.UserMapper;
import com.chasion.entity.User;
import com.chasion.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.digest.MD5;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    // 根据用户id查询用户名
    public UserDTO findUserById(int id){
        UserDTO userDTO = new UserDTO();
        // 从缓存中获取
        User user = getCache(id);
        if (user == null){
            user  = initCache(id);
        }
        BeanUtils.copyProperties(user,userDTO);
        return userDTO;
    }

    // 根据用户名查询用户
    public UserDTO findUserByUsername(String username){
        // 这里也应该从缓存中查找的
        UserDTO userDTO = new UserDTO();
        User user = userMapper.selectByName(username);
        if(user != null){
            BeanUtils.copyProperties(user,userDTO);
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
        user.setPassword(CommunityUtil.Md5(password + user.getSalt()));
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
            clearCache(userId);
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
//            System.out.println("inputPassword:"+inputPassword);
//            System.out.println("savePassword:"+savePassword);
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
                String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
                redisTemplate.opsForValue().set(ticketKey, loginTicket);
//                loginTicketMapper.insertLoginTicket(loginTicket);
                // 给客户发送ticket
                map.put("ticket", loginTicket.getTicket());
            }
        }
        return map;
    }

    // 退出登录
    public void logout(String ticket){
        // 在redis里的
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        // 在mysql里的，更新状态就完事了
//        loginTicketMapper.updateLoginTicket(ticket, 1);
    }

    // 获取用户的登录凭证
    public LoginTicketDTO getLoginTicket(String ticket){
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        LoginTicketDTO loginTicketDTO = new LoginTicketDTO();
        if(loginTicket != null){
            loginTicketDTO.setTicket(loginTicket.getTicket());
            loginTicketDTO.setUserId(loginTicket.getUserId());
            loginTicketDTO.setStatus(loginTicket.getStatus());
            loginTicketDTO.setExpired(loginTicket.getExpired());
            loginTicketDTO.setId(loginTicket.getId());
        }
        return loginTicketDTO;
    }

    // 更新用户头像地址
    // 更新头像
    public int updateHeader(int userId, String headerUrl){
        int i = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return i;
    }

    /**
     *   修改密码
     *   前端输入：原始密码，新密码和确认密码
     *   原始密码要和数据库中的密码进行校验
     *
     * */
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword, String confirmPassword){
//        System.out.println("oldPassword:"+oldPassword);
//        System.out.println("newPassword:"+newPassword);
//        System.out.println("confirmPassword:"+confirmPassword);
        HashMap<String, Object> map = new HashMap<>();
        // 对传入的参数进行校验
        if (StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg", "原始密码为空");
            return map;
        }else if (StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg", "新密码为空");
            return map;
        }else if (StringUtils.isBlank(confirmPassword)){
            map.put("confirmPasswordMsg", "确认密码为空");
            return map;
        }
        // 前端页面校验
//        if (oldPassword.length() < 8){
//            map.put("oldPasswordMsg", "密码长度小于8");
//            return map;
//        }
//        if (newPassword.length() < 8){
//            map.put("newPasswordMsg", "密码长度小于8");
//            return map;
//        }
        // 根据id取用用户        ？用户校验，此时是登录状态，所以用户是存在的
        User user = userMapper.selectById(userId);
        // 取数据库中的用户密码
        String password = user.getPassword();
        oldPassword = CommunityUtil.Md5(oldPassword + user.getSalt());
        if (password.equals(oldPassword)){
            // 与用户输入原始密码一致，进行修改，这里一般都有验证码的要求
            if (newPassword.equals(confirmPassword)){
                userMapper.updatePassword(userId,CommunityUtil.Md5(newPassword + user.getSalt()));
                clearCache(userId);
            }else {
                map.put("confirmPasswordMsg", "两次密码输入不一致");
            }

        }
        return map;
    }

    // 邮箱校验
    public Map<String, Object> checkEmail(String email){
        HashMap<String, Object> map = new HashMap<>();
        if (email == null ||StringUtils.isBlank(email)){
            map.put("msg", "邮箱不能为空!");
            return map;
        }
        User user = userMapper.selectByEmail(email);
        if (user == null){
            map.put("msg", "用户不存在!");
            return map;
        }
        return map;
    }

    // 忘记密码，修改密码
    public Map<String, Object> forgetPassword(String email, String password){
        HashMap<String, Object> map = new HashMap<>();
        // 邮箱已经校验过了
        User user = userMapper.selectByEmail(email);
        if (user == null){
            map.put("msg", "找不到该用户!");
        }else {
            userMapper.updatePassword(user.getId(), CommunityUtil.Md5(password + user.getSalt()));
            clearCache(user.getId());
        }
        return map;

    }

    // 1. 优先从缓存中取值
    private User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    // 2. 取不到数据时，初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3. 数据变更时，清除缓存数据
    private void clearCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
