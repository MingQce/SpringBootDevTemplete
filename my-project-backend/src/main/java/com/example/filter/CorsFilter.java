package com.example.filter;

import com.example.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Const.ORDER_CORS)  //指定优先级:springSecurity自带的过滤器链默认为最高优先级，为解决跨域问题需要让该过滤器的优先级更高
public class CorsFilter extends HttpFilter {   //因为要进行限流操作，所以要自定义CorsFilter来解决跨域问题

    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        this.addCorsHeader(request, response);  //调用方法处理跨域信息
        chain.doFilter(request, response);  //放行所有请求

    }

    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {

        response.addHeader("Access-Control-Allow-Origin", "http://localhost:5173"); //设定允许跨域访问的地址  request.getHeader("Origin")全部放开
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");  //允许使用的方法
        response.addHeader("Access-Control-Allow-Headers","Authorization, Content-Type");   //请求头中允许带有的信息

    }
}
