//package com.yirancrazy.smartmedical.manager.security;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.yirancrazy.smartmedical.annotation.Manager;
//import com.yirancrazy.smartmedical.mapper.AccountMapper;
//import com.yirancrazy.smartmedical.pojo.Account;
//import com.yirancrazy.smartmedical.pojo.CustomUserDetail;
//import com.yirancrazy.smartmedical.service.AccountService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description:
// * @Datetime: 2026-02-02 13:37
// * @Version: 1.0
// */
//
//@Manager
//@RequiredArgsConstructor
//public class DatabaseUserDetailsManager implements UserDetailsService {
//
//
//    private final AccountService accountService;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Account account = accountService.getAccountByPhone(username);
//        if (account == null) {
//            return null; // 如果用户查不到，返回null,由provider来抛出异常
//        }
//
//        System.out.println(account);
//
//
//        return User
//                .withUsername(account.getPhone())
//                .password(account.getPassword())
//                .authorities("p1")
//                .build();
//    }
//}