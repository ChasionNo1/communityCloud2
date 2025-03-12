package com.chasion.utils;

import com.chasion.entity.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {

    // 持有用户信息，用于代替session对象
    private ThreadLocal<UserDTO> users = new ThreadLocal<>();
    public void setUser(UserDTO user) {
        users.set(user);
    }
    public UserDTO getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}
