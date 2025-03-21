package com.chasion.apis;

import com.chasion.entity.FollowListDTO;
import com.chasion.entity.LoginTicketDTO;
import com.chasion.entity.UserDTO;
import com.chasion.resp.ResultData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FeignClient(name = "cloud-user", path = "/userService")
public interface UserFeignApi {

    @GetMapping("/getUser/id/{id}")
    public UserDTO findUserById(@PathVariable("id") int id);

    @PostMapping("/register")
    public ResultData<HashMap<String, Object>> register(@RequestParam("username") String username,
                                                        @RequestParam("password") String password,
                                                        @RequestParam("email") String email);

    @GetMapping("/getUser/username/{username}")
    public UserDTO findUserByUsername(@PathVariable("username") String username);

    @GetMapping("/activation/{userId}/{code}")
    public ResultData<Integer> activation(@PathVariable("userId") int userId, @PathVariable("code") String code);

    @PostMapping("/login")
    public ResultData<Map<String, Object>> login(@RequestParam("username") String username,
                                                 @RequestParam("password") String password,
                                                 @RequestParam("expired") int expired);

    @GetMapping("/logout")
    public String logout(@RequestParam("ticket") String ticket);


    @GetMapping("/get/loginTicket")
    public ResultData<LoginTicketDTO> getTicket(@RequestParam("ticket") String ticket);

    @PostMapping("/update/headerUrl")
    public ResultData<String> updateHeaderUrl(@RequestParam("userId") int userId, @RequestParam("headerUrl") String headerUrl);

    @PostMapping("/update/password")
    public ResultData<Map<String, Object>> updatePassword(@RequestParam("userId") int userId,
                                                          @RequestParam("oldPassword") String oldPassword,
                                                          @RequestParam("newPassword") String newPassword,
                                                          @RequestParam("confirmPassword") String confirmPassword);

    @GetMapping("/check/email")
    public ResultData<String> checkEmail(@RequestParam("email") String email);

    @PostMapping("/forget/password")
    public ResultData<String> forgetPassword(@RequestParam("email") String email, @RequestParam("password")String password);

    @PostMapping("/like")
    public ResultData<HashMap<String, Object>> like(@RequestParam("userId") int userId,
                                                    @RequestParam("entityType") int entityType,
                                                    @RequestParam("entityId") int entityId,
                                                    @RequestParam("entityUserId")int entityUserId,
                                                    @RequestParam("postId") int postId);

    @GetMapping("/get/likeCount")
    public ResultData<HashMap<String, String>> getLikeCount(@RequestParam(value = "userId", required = false) Integer userId,
                                                            @RequestParam("entityType") int entityType,
                                                            @RequestParam("entityId") int entityId);

    @GetMapping("/get/user/likeCount")
    public int getUserLikeCount(@RequestParam("userId") int userId);

    @PostMapping("/follow/user")
    public ResultData<Object> follow(@RequestParam("userId") int userId,
                                     @RequestParam("entityType") int entityType,
                                     @RequestParam("entityId") int entityId);

    @PostMapping("/unfollow/user")
    public ResultData<Object> unfollow(@RequestParam("userId") int userId,
                                       @RequestParam("entityType") int entityType,
                                       @RequestParam("entityId") int entityId);

    @GetMapping("/get/followeeCount")
    public long getFolloweeCount(@RequestParam("userId") int userId, @RequestParam("entityType") int entityType);

    @GetMapping("/get/followerCount")
    public long getFollowerCount(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId);

    @GetMapping("/get/isFollowed")
    public boolean getIsFollowed(@RequestParam("userId") int userId,
                                 @RequestParam("entityType") int entityType,
                                 @RequestParam("entityId") int entityId);

    @GetMapping("/get/followeeList")
    public ResultData<List<FollowListDTO>> getFolloweeList(@RequestParam("userId") int userId,
                                                           @RequestParam("entityType") int entityType,
                                                           @RequestParam("offset") int offset,
                                                           @RequestParam("limit") int limit);

    @GetMapping("/get/followerList")
    public ResultData<List<FollowListDTO>> getFollowerList(@RequestParam("entityType") int entityType,
                                                                 @RequestParam("entityId") int entityId,
                                                                 @RequestParam("offset") int offset,
                                                                 @RequestParam("limit") int limit);
}
