package com.example.config;

import com.auth0.jwt.JWT;
import com.example.entity.RestBean;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.filter.JWTAuthorizeFilter;
import com.example.utils.JWTUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
public class SecurityConfiguration {//配置SpringSecurity

    @Resource
    JWTUtils utils;

    @Resource
    JWTAuthorizeFilter jwtAuthorizeFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http  //新版本要求使用lambda表达式进行配置
                .authorizeHttpRequests(conf -> conf //配置请求验证
                        .requestMatchers("/api/auth/**").permitAll()  //放行登录相关请求
                        .anyRequest().authenticated()  //其他所有请求都需要经过验证
                )
                .formLogin(conf -> conf  //配置登录
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(this::onAuthenticationSuccess)  //自定义验证成功or失败后返回的数据(只有一个方法，所以写在外面并进行引用)
                        .failureHandler(this::onAuthenticationFailure)  //同上
                )
                .logout(conf -> conf //配置登出
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess)  //同名原方法似乎过时了，不过这里用的是自定义的
                )
                .exceptionHandling(conf -> conf  //错误访问处理
                        .authenticationEntryPoint(this::onUnauthorized)  //处理未登录状态
                        .accessDeniedHandler(this::onAccessDeny)  //处理当前用户角色没有访问权限的情况
                )
                .csrf(AbstractHttpConfigurer::disable)  //关了，安全性很高，但没必要
                .sessionManagement(conf -> conf  //状态管理
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //因为使用JWT令牌实现前后端分离，所以改成无状态
                )
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)  //忘了，复习完再补注释
                .build();
    }


    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws  IOException, ServletException {

    }

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException{
        response.setContentType("application/json");  //声明返回数据格式
        response.setCharacterEncoding("utf-8");  //设置编码格式
        User user = (User) authentication.getPrincipal();  //获取用户详细信息(UserDetails)
        String token = utils.createJWT(user, 1, "小明");  //调用工具类根据传入参数生成jwt令牌
        AuthorizeVO vo = new AuthorizeVO();
        vo.setExpire(JWT.decode(token).getExpiresAt());  //设置过期时间
        vo.setRole("");  //设置角色
        vo.setToken(token);
        vo.setUsername("小明");
        response.getWriter().write(RestBean.success(vo).asJsonString());  //返回json格式成功信息
    }

    public void onUnauthorized(HttpServletRequest request,
                               HttpServletResponse response,
                               AuthenticationException exception)throws IOException{
        response.setContentType("application/json");  //声明返回数据格式
        response.setCharacterEncoding("utf-8");  //设置编码格式
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
    }

    public void onAccessDeny(HttpServletRequest request,
                             HttpServletResponse response,
                             AccessDeniedException exception)throws IOException{
        response.setContentType("application/json");  //声明返回数据格式
        response.setCharacterEncoding("utf-8");  //设置编码格式
        response.getWriter().write(RestBean.forbidden(exception.getMessage()).asJsonString());
    }

    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json");  //声明返回数据格式
        response.setCharacterEncoding("utf-8");  //设置编码格式
        response.getWriter().write(RestBean.failure(401, exception.getMessage()).asJsonString());  //返回json格式失败信息(401,错误信息)
    }


}
