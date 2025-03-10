package com.chasion.cloudcommonsapi.apis;

import com.chasion.cloudcommonsapi.entity.UserDTO;
import com.chasion.cloudcommonsapi.resp.ResultData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@FeignClient(name = "cloud-user")
public interface UserFeignApi {

    @GetMapping("/getUser/{id}")
    @ResponseBody
    public UserDTO findUserById(@PathVariable("id") int id);

    @PostMapping("/userService/register")
    @ResponseBody
    public ResultData<HashMap<String, Object>> register(@RequestParam("username") String username,
                                                        @RequestParam("password") String password,
                                                        @RequestParam("email") String email);
}
