package com.chasion.entity;

import lombok.Data;

import java.util.Date;

@Data
public class UserDTO {
    private int id;
    private String username;
    private String email;
    private int type;
    private int status;
    private String headerUrl;
    private Date createTime;
    private String activationCode;
}
