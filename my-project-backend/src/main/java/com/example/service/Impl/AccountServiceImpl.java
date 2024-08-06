package com.example.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    FlowUtils utils;
    @Resource
    AmqpTemplate amqpTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    PasswordEncoder encoder;
    @Resource
    FlowUtils flow;

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
    @Override
    public String registerEmailVerifyCode(String type,String email,String ip){//注册邮件
        synchronized (ip.intern()) {//防止同一时间被多次调用
            if (!this.verifyLimit(ip)) {
                return "请求频繁，请稍后再试";
            }
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;//生成6位验证码
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", data);//格式转换和发送
            stringRedisTemplate.opsForValue()//将验证码存入redis
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }
    }

    /**
    判断账户是否注册过
    判断用户名是否重复
     **/
    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {//邮箱注册
        String email = vo.getEmail();
        String username = vo.getUsername();
        String key = Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(key);//从Redis中取
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码输入错误，请重新输入";
        if (this.existsAccountByEmail(email)) return "此电子邮件已被其他用户注册";
        if (this.existsAccountByUsername(username)) return "此用户名已被使用";
        String password = encoder.encode(vo.getPassword());
        Account account = new Account(null, username, password, email, "user", new Date());
        if (this.save(account)) {
            stringRedisTemplate.delete(key);
            return null;
        } else {
            return "内部错误,请联系管理员";
        }
    }

    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username", text).or()  //查到了对应的用户名或邮箱则......
                .eq("email", text)
                .one();
    }

    private boolean existsAccountByEmail(String email){//判断邮箱是否已被注册
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email",email));
    }
    private boolean existsAccountByUsername(String username){//判断用户名是否已被使用
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username",username));
    }
    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return utils.limitOnceCheck(key,60);
    }
}
