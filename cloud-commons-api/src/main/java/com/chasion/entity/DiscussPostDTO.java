package com.chasion.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DiscussPostDTO {
    private int id;
    private int userId;
    private String title;
    private String content;
    private int type;
    private int status;
    private Date createTime;
    private int commentCount;
    private double score;
}
