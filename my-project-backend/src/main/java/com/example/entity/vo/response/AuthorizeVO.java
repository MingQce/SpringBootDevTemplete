package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class AuthorizeVO {  //用户信息实体类
    String username;
    String role;
    String token;
    Date expire;
}
