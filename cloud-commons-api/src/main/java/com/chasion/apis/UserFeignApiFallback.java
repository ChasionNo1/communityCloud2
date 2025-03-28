package com.chasion.apis;

import com.chasion.entity.FollowListDTO;
import com.chasion.entity.LoginTicketDTO;
import com.chasion.entity.UserDTO;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserFeignApiFallback implements UserFeignApi {
    @Override
    public UserDTO findUserById(int id) {
        System.out.println("调用失败");
        return null;
    }

    @Override
    public ResultData<HashMap<String, Object>> register(String username, String password, String email) {
        return null;
    }

    @Override
    public UserDTO findUserByUsername(String username) {
        return null;
    }

    @Override
    public ResultData<Integer> activation(int userId, String code) {
        return null;
    }

    @Override
    public ResultData<Map<String, Object>> login(String username, String password, int expired) {
        return null;
    }

    @Override
    public String logout(String ticket) {
        return "";
    }

    @Override
    public ResultData<LoginTicketDTO> getTicket(String ticket) {
        return null;
    }

    @Override
    public ResultData<String> updateHeaderUrl(int userId, String headerUrl) {
        return null;
    }

    @Override
    public ResultData<Map<String, Object>> updatePassword(int userId, String oldPassword, String newPassword, String confirmPassword) {
        return null;
    }

    @Override
    public ResultData<String> checkEmail(String email) {
        return null;
    }

    @Override
    public ResultData<String> forgetPassword(String email, String password) {
        return null;
    }

    @Override
    public ResultData<HashMap<String, Object>> like(int userId, int entityType, int entityId, int entityUserId, int postId) {
        return null;
    }

    @Override
    public ResultData<HashMap<String, String>> getLikeCount(Integer userId, int entityType, int entityId) {
        return null;
    }

    @Override
    public int getUserLikeCount(int userId) {
        return 0;
    }

    @Override
    public ResultData<Object> follow(int userId, int entityType, int entityId) {
        ResultData<Object> resultData = new ResultData<>();
        resultData.setCode(ReturnCodeEnum.RC201.getCode());
        resultData.setMessage(ReturnCodeEnum.RC201.getMessage());
        return resultData;
    }

    @Override
    public ResultData<Object> unfollow(int userId, int entityType, int entityId) {
        return null;
    }

    @Override
    public long getFolloweeCount(int userId, int entityType) {
        return 0;
    }

    @Override
    public long getFollowerCount(int entityType, int entityId) {
        return 0;
    }

    @Override
    public boolean getIsFollowed(int userId, int entityType, int entityId) {
        return false;
    }

    @Override
    public ResultData<List<FollowListDTO>> getFolloweeList(int userId, int entityType, int offset, int limit) {
        return null;
    }

    @Override
    public ResultData<List<FollowListDTO>> getFollowerList(int entityType, int entityId, int offset, int limit) {
        return null;
    }

    @Override
    public ResultData<String> getPassword(int userId) {
        return null;
    }
}
