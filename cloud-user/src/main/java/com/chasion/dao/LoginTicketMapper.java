package com.chasion.dao;

import com.chasion.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
//@Deprecated
public interface LoginTicketMapper {

    // insert
    @Insert("insert into login_ticket (user_id, ticket, status, expired) " +
            "values (#{userId}, #{ticket}, #{status}, #{expired})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    // select
    @Select("select id, user_id, ticket, status, expired from login_ticket where ticket = #{ticket}")
    LoginTicket selectLoginTicket(String ticket);

    // update
    @Update("update login_ticket set status = #{status} where ticket = #{ticket}")
    int updateLoginTicket(@Param("ticket") String ticket, @Param("status")int status);
}
