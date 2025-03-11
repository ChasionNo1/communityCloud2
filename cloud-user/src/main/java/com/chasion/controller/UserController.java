package com.chasion.controller;

import com.chasion.entity.UserDTO;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/userService")
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



}
