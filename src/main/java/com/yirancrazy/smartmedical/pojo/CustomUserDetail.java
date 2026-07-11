package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 自定义 Spring Security UserDetails
 * @Author: YiRanCrazy@gmail.com
 * @Description: 适配 account 表结构，登录态主体使用 phone 作为 username
 * @Datetime: 2026-02-02 13:42
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetail implements UserDetails {
    @TableId
    private String accountId;
    @TableField("user_id")
    private String userId;
    @TableField("role_id")
    private String roleId;
    private String password;
    private String email;
    private String phone;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return phone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}