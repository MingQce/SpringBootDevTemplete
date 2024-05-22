package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {  //查自定义的用户信息
        Account account = this.findAccountByNameOrEmail(username);  //参数名为username，但因为设计用户可以用邮箱登录
        if(account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)  //这里用username而不是account,因为account有可能是邮箱
                .password(account.getPassword())
                .roles(account.getRole())
                .build();

    }

    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username", text).or()  //查到了对应的用户名或邮箱则......
                .eq("email", text)
                .one();
    }
}
