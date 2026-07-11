package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.RoleConstant;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.vo.LoginVo;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.NicknameGenerator;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yirancrazy.smartmedical.constant.RoleConstant.ROLE_LIST;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
@Slf4j
public class AuthManager {

    @Value("${jwt.accessSecretKey}")
    private String accessSecretKey;
    @Value("${jwt.refreshSecretKey}")
    private String refreshSecretKey;
    @Value("${cookie.secure:false}")
    private boolean cookieSecure;
    private final AccountService accountService;
    private final UserService userService;
    private final RedisUtil redisUtil;
    private final PatientCardService patientCardService;
    private final PatientService patientService;
    private final Long USER_ROLE = 1L;

    public Result<LoginVo> login(String phone, String password, HttpServletResponse response) {
        try{

            List<Account> accountByPhone = accountService.getAccountByPhone(phone);

            // 过滤出用户账户
            Account account = accountByPhone
                    .stream()
                    .filter(account1 -> USER_ROLE
                            .equals(account1.getRoleId()))
                    .findFirst()
                    .orElse(null);

            if(account==null){
                return Result.info(10001,"账号不存在", null);
            }
            if(!BCrypt.checkpw(password, account.getPassword())){
                return Result.info(10002,"用户名或密码错误", null);
            }
            User user = userService.getUserById(account.getUserId());

            Map<String, Object> accessHeader = new HashMap<>();
            accessHeader.put("alg", "HS256");
            accessHeader.put("typ", "JWT");
            Map<String, Object> accessPayload = new HashMap<>();
            accessPayload.put(JWTPayload.ISSUER, "YiRanCrazy");
            accessPayload.put(JWTPayload.SUBJECT, account.getId().toString());
            accessPayload.put("role_id", account.getRoleId());
            Long currentTimeMillis = System.currentTimeMillis();
            accessPayload.put(JWTPayload.EXPIRES_AT, currentTimeMillis + 1000 * 60 * 60 * 24 *7);
            accessPayload.put(JWTPayload.NOT_BEFORE, currentTimeMillis);
            accessPayload.put(JWTPayload.ISSUED_AT, currentTimeMillis);
            accessPayload.put(JWTPayload.JWT_ID, String.valueOf(IdUtil.getSnowflakeNextId()));
            String accessJwt = JWTUtil.createToken(accessHeader, accessPayload, accessSecretKey.getBytes());

            redisUtil.setEx("access_token_"+account.getId().toString(),accessJwt,7, TimeUnit.DAYS);

            Map<String, Object> refreshHeader = new HashMap<>();
            refreshHeader.put("alg", "HS256");
            refreshHeader.put("typ", "JWT");
            Map<String, Object> refreshPayload = new HashMap<>();
            refreshPayload.put(JWTPayload.ISSUER, "YiRanCrazy");
            refreshPayload.put(JWTPayload.SUBJECT, account.getId().toString());
            refreshPayload.put(JWTPayload.EXPIRES_AT, currentTimeMillis + 1000 * 60 * 60 * 24 * 7);
            refreshPayload.put(JWTPayload.NOT_BEFORE, currentTimeMillis);
            refreshPayload.put(JWTPayload.ISSUED_AT, currentTimeMillis);
            refreshPayload.put(JWTPayload.JWT_ID, String.valueOf(IdUtil.getSnowflakeNextId()));
            String refreshJwt = JWTUtil.createToken(refreshHeader, refreshPayload, refreshSecretKey.getBytes());

            redisUtil.setEx("refresh_token_"+account.getId().toString(),refreshJwt,7, TimeUnit.DAYS);

            LoginVo loginVo = new LoginVo(String.valueOf(account.getId()), accessJwt, String.valueOf(user.getId()));

            response.setHeader("Authorization", "Bearer " + accessJwt);

            Cookie cookie = new Cookie("Refresh-token", refreshJwt);
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setPath("/api");
            cookie.setHttpOnly(true);
            cookie.setSecure(cookieSecure);
            response.addCookie(cookie);
            logInitialize(user);

            return Result.success(loginVo);
        } catch (Exception e){
            log.error("登录异常", e);
            return Result.fail("登录失败");
        }
    }

    private void logInitialize(User user) {
        redisUtil.setEx("uid_"+String.valueOf(user.getId()),user.getId().toString(),30, TimeUnit.DAYS);

    }

    @Transactional
    public Result<String> register(String phone, String password) {

        if (accountService.getAccountByPhone(phone) != null && !accountService.getAccountByPhone(phone).isEmpty()) {
            return Result.info(10001,"账号已存在", null);
        }

        User user = new User();
        user.setId(IdUtil.getSnowflakeNextId());
        user.setUsername(NicknameGenerator.generateRandomNickname());
        user.setAvatar("");
        userService.insertUser(user);



        Account account = new Account();
        account.setId(IdUtil.getSnowflakeNextId());
        account.setUserId(user.getId());
        account.setRoleId(USER_ROLE);
        account.setPhone(phone);
        account.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        accountService.insertAccount(account);

        registerInit(user.getId());

        return Result.success("注册成功");
    }

    public Result<String> logout(Long userId) {
        redisUtil.delete("access_token_" + userId);
        redisUtil.delete("refresh_token_" + userId);
        return Result.success("登出成功");
    }

    private void registerInit(Long userId){
        PatientCard patientCard = new PatientCard();
        patientCard.setId(IdUtil.getSnowflakeNextId());
        patientCard.setSn(IdUtil.getSnowflakeNextId());
        patientCard.setInpatientBalance(0);
        patientCard.setOutpatientBalance(0);
        patientCard.setPaymentPassword("");
        patientCard.setQrCode("");
        patientCard.setStatus(0);
        patientCardService.insertPatientCard(patientCard);

        Patient patient = new Patient();
        patient.setId(IdUtil.getSnowflakeNextId());
        patient.setUserId(userId);
        patient.setPatientCardId(patientCard.getId());
        patientService.insertPatient(patient);
    }
}
