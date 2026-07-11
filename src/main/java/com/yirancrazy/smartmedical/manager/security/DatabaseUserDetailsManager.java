package com.yirancrazy.smartmedical.manager.security;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自定义 UserDetailsService：从 account 表按手机号加载登录主体
 * @Author: YiRanCrazy@gmail.com
 * @Description: 配合 JwtAuthenticationFilter 做 token 颁发前的账号校验
 * @Datetime: 2026-02-02 13:37
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseUserDetailsManager implements UserDetailsService {

    private final AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        List<Account> accounts = accountService.getAccountByPhone(phone);
        if (accounts == null || accounts.isEmpty()) {
            log.warn("账号不存在：phone={}", phone);
            throw new UsernameNotFoundException("账号不存在");
        }
        Account account = accounts.get(0);
        log.debug("加载账号成功：phone={}, roleId={}", account.getPhone(), account.getRoleId());
        return User.withUsername(account.getPhone())
                .password(account.getPassword())
                .authorities("ROLE_USER")
                .build();
    }
}