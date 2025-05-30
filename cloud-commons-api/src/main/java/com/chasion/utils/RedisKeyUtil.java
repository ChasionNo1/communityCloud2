package com.chasion.utils;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";



    // 某个实体的赞
    // like:entity:entityType:entityId->set(userId)
    public static String getPrefixEntityLike(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    public static String getPrefixUserLike(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体
    // followee:userId:entityType  -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE  + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码的key
    public static String getKaptchaKey(String owner){
        // 用户在登录时，还获取不到登录的userid，因此临时发放一个凭证
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 获取存放user的key
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日uv
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    // 区间uv
    public static String getuvKey(String startDate, String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日dau
    public static String getDauKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间dau
    public static String getDauKey(String startDate, String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }
}
