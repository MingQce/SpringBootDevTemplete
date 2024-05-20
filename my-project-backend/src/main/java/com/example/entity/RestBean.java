package com.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public record RestBean<T>(int code, T data, String message) {  //使用记录类型，把返回信息封装成实体类再返回(请求码， 数据， 信息)
    public static <T> RestBean<T> success(T data){  //工具方法-成功
        return new RestBean<>(200, data, "请求成功");
    }

    public static <T> RestBean<T> success(){  //成功但没有数据
        return success(null);
    }
    public static <T> RestBean<T> unauthorized(String message){
        return failure(401, message);
    }
    public static <T> RestBean<T> forbidden(String message){
        return failure(403, message);
    }
    public static <T> RestBean<T> failure(int code, String message){
        return new RestBean<>(code, null, message);
    }
    public String asJsonString(){  //把数据转为json格式(使用fastjson2)
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
