package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FlowUtils {
    @Resource
    StringRedisTemplate template;

    public boolean limitOnceCheck(String key, int blockTime){
        if(Boolean.TRUE.equals(template.hasKey(key))){//如果已经拿到了key，不能再次获取
            return false;
        } else {//存入key，进行冷却
            template.opsForValue().set(key,"",blockTime, TimeUnit.SECONDS);
            return true;
        }
    }
}
