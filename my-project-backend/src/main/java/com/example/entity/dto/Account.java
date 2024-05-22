package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
@TableName("db_account")  //映射数据库中对应的表
@AllArgsConstructor  //自动生成构造方法
@Data  //get,set等方法...
public class Account {
    @TableId(type = IdType.AUTO)  //声明主键
    Integer id;  //需要使用包装类
    String username;
    String password;
    String email;
    String role;
    LocalDateTime registerTime;

}
