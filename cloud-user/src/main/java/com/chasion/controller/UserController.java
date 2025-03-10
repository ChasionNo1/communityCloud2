package com.chasion.clouduser.controller;

import com.chasion.cloudcommonsapi.entity.UserDTO;
import com.chasion.cloudcommonsapi.resp.ResultData;
import com.chasion.cloudcommonsapi.resp.ReturnCodeEnum;
import com.chasion.clouduser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/getUser/{id}")
    public UserDTO findUserById(@PathVariable("id") int id){
        return userService.findUserById(id);
    }


    // 响应openfeign的注册请求
    @PostMapping("/userService/register")
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



}
