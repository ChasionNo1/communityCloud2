package com.chasion.controller;


import com.chasion.entity.LoginTicketDTO;
import com.chasion.entity.User;
import com.chasion.entity.UserDTO;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/userService")
@RefreshScope
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/getUser/id/{id}")
    public UserDTO findUserById(@PathVariable("id") int id){
        return userService.findUserById(id);
    }

    @GetMapping("/getUser/username/{username}")
    public UserDTO findUserByUsername(@PathVariable("username") String username){
        return userService.findUserByUsername(username);
    }


    // 响应openfeign的注册请求
    @PostMapping("/register")
    public ResultData<HashMap<String, Object>> register(@RequestParam("username") String username,
                                                        @RequestParam("password") String password,
                                                        @RequestParam("email") String email) {
        // 调用 service进行注册，得到注册结果
        HashMap<String, Object> map = userService.register(username, password, email);
        ResultData<HashMap<String, Object>> resultData = new ResultData<>();
        if (map == null || map.isEmpty()) {
            resultData.setCode(ReturnCodeEnum.RC200.getCode());
            resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
        }
        resultData.setData(map);
        return resultData;

    }

    // 激活服务
    @GetMapping("/activation/{userId}/{code}")
    public ResultData<Integer> activation(@PathVariable("userId") int userId, @PathVariable("code") String code) {
        int activationStatus = userService.activation(userId, code);
        ResultData<Integer> resultData = new ResultData<>();
        // 重复激活
        if (activationStatus == 1) {
            resultData.setCode(ReturnCodeEnum.RC999.getCode());
            resultData.setMessage("激活失败，重复激活");
            resultData.setData(activationStatus);
        }else if (activationStatus == 2) {
            // 激活失败
            resultData.setCode(ReturnCodeEnum.RC999.getCode());
            resultData.setMessage("激活失败，信息有误");
            resultData.setData(activationStatus);
        }else {
            resultData.setCode(ReturnCodeEnum.RC200.getCode());
            resultData.setMessage("激活成功，可以登录了");
            resultData.setData(activationStatus);
        }
        return resultData;
    }

    // 登录请求
    @PostMapping("/login")
    public ResultData<Map<String, Object>> login(@RequestParam("username") String username,
                                    @RequestParam("password") String password,
                                    @RequestParam("expired") int expired) {
        Map<String, Object> map = userService.login(username, password, expired);
        ResultData<Map<String, Object>> resultData = new ResultData<>();
        resultData.setData(map);
        return resultData;
    }

    // 退出登录
    @GetMapping("/logout")
    public String logout(@RequestParam("ticket") String ticket) {
        userService.logout(ticket);
        return "ok";
    }


    // 验证登录ticket的有效性
    @GetMapping("/get/loginTicket")
    public ResultData<LoginTicketDTO> getTicket(@RequestParam("ticket") String ticket) {
        ResultData<LoginTicketDTO> resultData = new ResultData<>();
        LoginTicketDTO loginTicket = userService.getLoginTicket(ticket);
        if (loginTicket == null) {
            resultData.setCode(ReturnCodeEnum.RC999.getCode());
            resultData.setMessage("没有获取到ticket");
        }else {
            resultData.setData(loginTicket);
        }
        return resultData;
    }

    // 更新头像地址
    @PostMapping("/update/headerUrl")
    public ResultData<String> updateHeaderUrl(@RequestParam("userId") int userId, @RequestParam("headerUrl") String headerUrl) {
        int i = userService.updateHeader(userId, headerUrl);
        ResultData<String> resultData = new ResultData<>();
        resultData.setCode(ReturnCodeEnum.RC200.getCode());
        return resultData;
    }

    // 修改密码  user.getId(), oldPassword, newPassword, confirmPassword
    @PostMapping("/update/password")
    public ResultData<Map<String, Object>> updatePassword(@RequestParam("userId") int userId,
                                             @RequestParam("oldPassword") String oldPassword,
                                             @RequestParam("newPassword") String newPassword,
                                             @RequestParam("confirmPassword") String confirmPassword) {
        Map<String, Object> map = userService.updatePassword(userId, oldPassword, newPassword, confirmPassword);
        ResultData<Map<String, Object>> resultData = new ResultData<>();
        if (map == null || map.isEmpty()) {
            resultData.setCode(ReturnCodeEnum.RC200.getCode());
            resultData.setMessage("密码修改成功!");
            resultData.setData(map);
        }else {
            resultData.setCode(ReturnCodeEnum.RC999.getCode());
            resultData.setMessage("密码修改失败!");
            resultData.setData(map);
        }
        return resultData;
    }

    // 检查用户邮箱情况
    @GetMapping("/check/email")
    public ResultData<String> checkAndSendEmail(@RequestParam("email") String email) {
        Map<String, Object> map = userService.checkEmail(email);
        ResultData<String> resultData = new ResultData<>();
        if (map == null || map.isEmpty()) {
            resultData.setCode(ReturnCodeEnum.RC200.getCode());
            resultData.setMessage("该邮箱是合法用户");
        }else {
            resultData.setCode(ReturnCodeEnum.RC999.getCode());
            resultData.setMessage(map.get("msg").toString());
        }
        return resultData;
    }

    // 忘记密码
    @PostMapping("/forget/password")
    public ResultData<String> forgetPassword(@RequestParam("email") String email, @RequestParam("password")String password) {
        Map<String, Object> map = userService.forgetPassword(email, password);
        ResultData<String> resultData = new ResultData<>();
        if (map == null || map.isEmpty()) {
            resultData.setCode(ReturnCodeEnum.RC200.getCode());
            resultData.setMessage("修改成功!");
        }else {
            resultData.setCode(ReturnCodeEnum.RC999.getCode());
            resultData.setMessage(map.get("msg").toString());
        }
        return resultData;
    }

    // getAuthorities(user.getId())
    // ！！！！这里不能调用到spring security
//    @GetMapping("/get/authorities")
//    public ResultData<Collection<? extends GrantedAuthority>> getAuthorities(@RequestParam("userId") int userId) {
//        Collection<? extends GrantedAuthority> authorities = userService.getAuthorities(userId);
//        return new ResultData<Collection<? extends GrantedAuthority>>().setData(authorities);
//    }

    // 获取用户密码，感觉是危险行为
    @GetMapping("/get/password")
    public ResultData<String> getPassword(@RequestParam("userId") int userId) {
        ResultData<String> resultData = new ResultData<>();
        User userWithPsdById = userService.findUserWithPsdById(userId);
        return resultData.setData(userWithPsdById.getPassword());
    }



}
