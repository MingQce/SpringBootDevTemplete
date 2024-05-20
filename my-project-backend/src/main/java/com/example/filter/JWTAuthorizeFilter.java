package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.JWTUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthorizeFilter extends OncePerRequestFilter {  //解析JWT令牌,自定义校验机制

    @Resource
    JWTUtils utils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {  //自定义验证逻辑
        String authorization = request.getHeader("Authorization");  //从请求头中获取该参数
        DecodedJWT jwt = utils.resolveJwt(authorization);  //检验令牌是否可用
        if(jwt != null){
            UserDetails user = utils.toUser(jwt);  //把jwt令牌内容转入UserDetails对象中
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);  //把信息传入SecurityContext进行验证
            request.setAttribute("id",utils.toId(jwt));
        }
        filterChain.doFilter(request,response);
    }
}
