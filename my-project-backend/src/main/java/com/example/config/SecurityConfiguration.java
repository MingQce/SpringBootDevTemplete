package com.example.config;

import com.auth0.jwt.JWT;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.filter.JWTAuthorizeFilter;
import com.example.service.AccountService;
import com.example.utils.JWTUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
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
import java.io.PrintWriter;

@Configuration
public class SecurityConfiguration {//配置SpringSecurity

    @Resource
    JWTUtils utils;

    @Resource
    JWTAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    AccountService service;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http  //新版本要求使用lambda表达式进行配置
                .authorizeHttpRequests(conf -> conf //配置请求验证
                        .requestMatchers("/api/auth/**","/error").permitAll()  //放行登录相关请求
                        .anyRequest().authenticated()  //其他所有请求都需要经过验证
                )
                .formLogin(conf -> conf  //配置登录
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(this::onAuthenticationSuccess)  //自定义验证成功后返回的数据(只有一个方法，所以写在外面并进行引用)
                        .failureHandler(this::onAuthenticationFailure)  //自定义验证失败后的处理
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
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)  //UsernamePasswordAuthenticationFilter是AbstractAuthenticationProcessingFilter针对使用用户名和密码进行身份认证而定制化的一个过滤器。其添加是在调用http.formLogin()时作用，默认的登录请求pattern为"/login"，并且为POST请求。当我们登录的时候，也就是匹配到loginProcessingUrl，这个过滤器就会委托认证管理器authenticationManager来验证登录
                .build();
    }

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException{
        response.setContentType("application/json");  //声明返回数据格式
        response.setCharacterEncoding("utf-8");  //设置编码格式
        User user = (User) authentication.getPrincipal();  //获取用户详细信息(UserDetails)
        Account account = service.findAccountByNameOrEmail(user.getUsername());
        String token = utils.createJWT(user, account.getId(), account.getUsername());  //调用工具类根据传入参数生成jwt令牌
        AuthorizeVO vo = account.asViewObject(AuthorizeVO.class, v -> {
            v.setExpire(JWT.decode(token).getExpiresAt());  //设置过期时间
            v.setToken(token);
        });  //自定义实现了下面的复制方法
//        BeanUtils.copyProperties(account, vo);  //拷贝前一个对象的参数给后一个对象,省略部分vo.set转移对象参数的功能
        response.getWriter().write(RestBean.success(vo).asJsonString());  //返回json格式成功信息
    }

    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json");  //声明返回数据格式
        response.setCharacterEncoding("utf-8");  //设置编码格式
        response.getWriter().write(RestBean.failure(401, exception.getMessage()).asJsonString());  //返回json格式失败信息(401,错误信息)
    }

    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws  IOException, ServletException {  //登出(使用redis黑名单方法)
        response.setContentType("application/json");  //声明返回数据格式
        response.setCharacterEncoding("utf-8");  //设置编码格式
        PrintWriter writer = response.getWriter();
        String authorization = request.getHeader("Authorization");
        if(utils.invalidateJwt(authorization)){
            writer.write(RestBean.success().asJsonString());
        }else {
            writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
        }
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




}
