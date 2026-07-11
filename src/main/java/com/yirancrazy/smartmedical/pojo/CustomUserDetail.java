//package com.yirancrazy.smartmedical.pojo;
//
//import com.baomidou.mybatisplus.annotation.FieldFill;
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableId;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.time.LocalDateTime;
//import java.util.Collection;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description:
// * @Datetime: 2026-02-02 13:42
// * @Version: 1.0
// */
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class CustomUserDetail implements UserDetails {
//    @TableId
//    private String accountId;
//    private String userId;
//    private String roleId;
//    private String password;
//    private String email;
//    private String phone;
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return null;
//    }
//
//    @Override
//    public String getPassword() {
//        return password;
//    }
//
//    @Override
//    public String getUsername() {
//        return email; // or phone, depending on your login strategy
//    }
//}