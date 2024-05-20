package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
public class JWTUtils {  //JWT令牌工具类
    @Value("${spring.security.jwt.key}")  //设置密钥
    String key;

    @Value("${spring.security.jwt.expire}")  //设置过期时间
    int expire;

    public DecodedJWT resolveJwt(String headerToken){  //取出header中的token，解析JWT
        String token = this.convertToken(headerToken);
        if(token == null) return null;  //获取到的token为空，验证失败
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);  //检验jwt令牌是否被用户篡改
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;  //检验jwt令牌是否过期
        } catch (JWTVerificationException e){
            return null;  //发现jwt令牌异常，验证失败
        }

    }
    public String createJWT(UserDetails details, int id, String username){  //创建令牌
        Algorithm algorithm = Algorithm.HMAC256(key);  //设置加密算法
        Date expire = this.expireTime();
        return JWT.create()
                .withClaim("id", id)  //自定义参数-id
                .withClaim("name", username)  //username
                .withClaim("authorities", details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())  //定义该用户的权限
                .withExpiresAt(expire) //自定义令牌过期时间
                .withIssuedAt(new Date())  //颁发时间
                .sign(algorithm);  //使用算法进行签名,得到最终的jwt令牌

    }

    public Date expireTime(){  //单独编写日期类型方法设置过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);  //暂时设置为七天方便开发，后续设置JWT令牌续签
        return calendar.getTime();
    }

    public UserDetails toUser(DecodedJWT jwt){  //把jwt令牌解析成userdetails中的信息
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())  //参数名同创建jwt时使用的
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    public Integer toId(DecodedJWT jwt){  //取出id
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }
    private String convertToken(String headerToken){  //处理获取到的token
        if(headerToken == null || !headerToken.startsWith("Bearer "))  //判断token是否合法
            return null;
        return headerToken.substring(7);  //去掉开头的"Bearer "七个字符
    }
}
