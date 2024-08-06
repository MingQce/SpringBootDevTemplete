package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.EmailRegisterVO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {  //接口可以多重继承
    Account findAccountByNameOrEmail(String text);  //要用方法得在父類里聲明
    String registerEmailVerifyCode(String type,String email,String ip);//需要区分邮件类型，限制用户请求频率
    String registerEmailAccount(EmailRegisterVO vo);
}
