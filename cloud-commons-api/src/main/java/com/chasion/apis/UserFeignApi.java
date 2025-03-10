package com.chasion.apis;

import com.chasion.entity.UserDTO;
import com.chasion.resp.ResultData;
import com.chasion.entity.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@FeignClient(name = "cloud-user")
public interface UserFeignApi {

    @GetMapping("/userService/getUser/id/{id}")
    @ResponseBody
    public UserDTO findUserById(@PathVariable("id") int id);

    @PostMapping("/userService/register")
    @ResponseBody
    public ResultData<HashMap<String, Object>> register(@RequestParam("username") String username,
                                                        @RequestParam("password") String password,
                                                        @RequestParam("email") String email);

    @GetMapping("/userService/getUser/username/{username}")
    public UserDTO findUserByUsername(@PathVariable("username") String username);

    @GetMapping("/userService/activation/{userId}/{code}")
    public ResultData<Integer> activation(@PathVariable("userId") int userId, @PathVariable("code") String code);
}
