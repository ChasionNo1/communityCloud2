package com.chasion.entity;

import lombok.Data;

import java.util.Date;

@Data
public class FollowListDTO {
    // 关注列表DTO
    private UserDTO user;
    private Date followTime;
    private boolean followed;
}
