package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.dto.user.request.UpdateUserProfileRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.request.UpdateUserPhoneRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.request.UserPhoneSmsCodeRequest;
import com.yirancrazy.smartmedical.pojo.vo.UserBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.UserInfoVo;
import com.yirancrazy.smartmedical.pojo.vo.UserProfileInfo;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.SmsService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.MinIOUtil;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户业务编排：用户 / 就诊人基础信息查询
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class UserManager {

    private static final int AVATAR_URL_EXPIRE_SECONDS = 7 * 24 * 60 * 60;

    private final UserService userService;

    private final AccountService accountService;

    private final RedisUtil redisUtil;

    private final SmsService smsService;

    @Value("${jwt.accessTokenPrefix}")
    private String accessTokenPrefix;

    @Value("${jwt.admin.adminRefreshTokenPrefix}")
    private String adminRefreshTokenPrefix;

    /**
     * 添加用户
     * @param user 用户
     * @return 添加结果
     */
    public int addUser(User user) {
        user.setId(IdUtil.getSnowflakeNextId());
        return userService.insertUser(user);
    }

    /**
     * 根据id获取用户
     * @param id 用户id
     * @return  用户
     */
    public User getUserById(Long id) {
        return userService.getUserById(id);
    }

    /**
     * 删除用户
     * @param id 用户id
     * @return 删除结果
     */
    public int deleteUserById(Long id) {
        return userService.deleteUserById(id);
    }

    /**
     * 获取用户详情
     * @param accountId 账号id
     * @return 用户详情
     */
    public Result<UserInfoVo> getUserDetailsById(Long accountId) {
        Account account = accountService.getAccountById(accountId);
        User user = userService.getUserById(account.getUserId());

        // 组装返回元素
        UserInfoVo userDetails = new UserInfoVo();
        userDetails.setAccountId(String.valueOf(account.getId()));
        userDetails.setUserId(String.valueOf(user.getId()));
        userDetails.setEmail(account.getEmail());
        userDetails.setPhone(account.getPhone());
        userDetails.setAvatar(resolveAvatarUrl(user.getAvatar()));
        userDetails.setSex(user.getSex());
        userDetails.setNickName(user.getUsername());

        return Result.success(userDetails);
    }

    /**
     * 获取用户基础信息
     * @param userId 账号id
     * @return 用户基础信息
     */
    public Result<UserBaseInfo> getUserBaseInfoByUserId(Long userId) {
        UserBaseInfo userBaseInfo = userService.getUserBaseInfoByUserId(userId);
        if (userBaseInfo != null) {
            userBaseInfo.setAvatar(resolveAvatarUrl(userBaseInfo.getAvatar()));
        }
        log.info("获取用户基础信息: {}", userBaseInfo);
        return Result.success(userBaseInfo);
    }

    /**
     * 获取当前登录用户的个人信息
     * @param userId 当前登录用户ID
     * @return 用户个人信息
     */
    public Result<UserProfileInfo> getUserProfile(Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.info(10001, "用户不存在", null);
        }
        Account account = accountService.getAccountByUserId(userId);
        return Result.success(toUserProfileInfo(user, account));
    }

    /**
     * 更新当前登录用户的个人信息并返回最新完善状态
     * @param userId 当前登录用户ID
     * @param request 个人信息请求
     * @return 更新后的用户个人信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<UserProfileInfo> updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.info(10001, "用户不存在", null);
        }
        user.setUsername(request.getUsername().trim());
        if (request.getNickname() != null) {
            String nickname = request.getNickname().trim();
            if (!StringUtils.hasText(nickname)) {
                return Result.fail("昵称不能为空");
            }
            user.setNickname(nickname);
        }
        user.setIdCard(request.getIdCard().trim().toUpperCase(Locale.ROOT));
        user.setSex(request.getSex());
        user.setAddress(StringUtils.hasText(request.getAddress()) ? request.getAddress().trim() : null);
        userService.updateUserById(user);
        return Result.success(toUserProfileInfo(user, accountService.getAccountByUserId(userId)));
    }

    /**
     * 上传并更新当前用户头像
     * @param userId 当前登录用户ID
     * @param file 图片文件
     * @return 头像 URL
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> uploadAvatar(Long userId, MultipartFile file) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.info(10001, "用户不存在", null);
        }
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择图片文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail("仅允许上传图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail("图片大小不能超过 5MB");
        }

        try {
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null) {
                int dot = original.lastIndexOf('.');
                if (dot >= 0 && dot < original.length() - 1) {
                    String raw = original.substring(dot + 1).toLowerCase(Locale.ROOT);
                    if (raw.matches("[a-z0-9]{1,8}")) {
                        ext = "." + raw;
                    }
                }
            }
            String objectName = "user/avatar/" + IdUtil.getSnowflakeNextId() + ext;
            MinIOUtil.uploadFile(MinIOUtil.getBucketName(), file, objectName, contentType);
            String basisUrl = MinIOUtil.getBasisUrl();
            if (basisUrl == null) {
                return Result.fail("MinIO 未配置，无法生成图片地址");
            }
            String avatarUrl = resolveAvatarUrl(objectName);
            if (avatarUrl == null) {
                return Result.fail("头像地址生成失败");
            }
            // 数据库只保存对象名；对外 URL 每次查询时生成预签名地址
            user.setAvatar(objectName);
            userService.updateUserById(user);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            log.error("[user-avatar] 头像上传失败, userId={}", userId, e);
            return Result.fail("图片上传失败");
        }
    }

    /**
     * 发送手机号换绑验证码
     * @param accountId 当前登录账号ID
     * @param request 手机号与场景
     * @return 发送结果
     */
    public Result<String> sendPhoneChangeCode(Long accountId, UserPhoneSmsCodeRequest request) {
        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            return Result.info(10001, "账号不存在", null);
        }

        String phone = request.getPhone();
        if ("old".equals(request.getScene())) {
            if (!phone.equals(account.getPhone())) {
                return Result.fail("旧手机号与当前账号不一致");
            }
        } else {
            if (phone.equals(account.getPhone())) {
                return Result.fail("新手机号不能与当前手机号相同");
            }
            List<Account> existing = accountService.getAccountByPhone(phone);
            if (existing != null && !existing.isEmpty()) {
                return Result.fail("该手机号已注册");
            }
        }
        return Result.success(smsService.sendCode(phone));
    }

    /**
     * 校验旧号和新号验证码并完成手机号换绑
     * @param accountId 当前登录账号ID
     * @param request 换绑请求
     * @return 换绑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> changePhone(Long accountId, UpdateUserPhoneRequest request) {
        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            return Result.info(10001, "账号不存在", null);
        }
        if (request.getNewPhone().equals(account.getPhone())) {
            return Result.fail("新手机号不能与当前手机号相同");
        }

        smsService.verifyCode(account.getPhone(), request.getOldPhoneCode());
        List<Account> existing = accountService.getAccountByPhone(request.getNewPhone());
        if (existing != null && !existing.isEmpty()) {
            return Result.fail("该手机号已注册");
        }
        smsService.verifyCode(request.getNewPhone(), request.getNewPhoneCode());

        account.setPhone(request.getNewPhone());
        try {
            accountService.updateAccountById(account);
        } catch (DuplicateKeyException e) {
            log.warn("[change-phone] 手机号唯一索引冲突, accountId={}", accountId);
            return Result.fail("该手机号已注册");
        }
        redisUtil.delete(accessTokenPrefix + accountId);
        redisUtil.delete(adminRefreshTokenPrefix + accountId);
        log.info("[change-phone] 手机号换绑成功, accountId={}", accountId);
        return Result.success("换绑成功，请重新登录");
    }

    /**
     * 组装用户个人信息并计算完善状态
     */
    private UserProfileInfo toUserProfileInfo(User user, Account account) {
        boolean profileCompleted = StringUtils.hasText(user.getUsername())
                && StringUtils.hasText(user.getIdCard())
                && user.getSex() != null;
        return new UserProfileInfo(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getNickname(),
                resolveAvatarUrl(user.getAvatar()),
                account == null ? null : account.getPhone(),
                user.getIdCard(),
                user.getSex(),
                user.getAddress(),
                profileCompleted);
    }

    /**
     * 将数据库中的对象名或历史外链转换为浏览器可访问的预签名 URL
     */
    private String resolveAvatarUrl(String avatar) {
        if (!StringUtils.hasText(avatar)) {
            return avatar;
        }
        String objectName = extractObjectName(avatar.trim());
        if (objectName.startsWith("http://") || objectName.startsWith("https://")) {
            return objectName;
        }
        try {
            return MinIOUtil.getPresignedObjectUrl(
                    MinIOUtil.getBucketName(), objectName, AVATAR_URL_EXPIRE_SECONDS);
        } catch (Exception e) {
            log.warn("[user-avatar] 生成预签名地址失败, objectName={}", objectName, e);
            return null;
        }
    }

    /**
     * 从历史完整 URL 或新对象名中提取 MinIO 对象名
     */
    private String extractObjectName(String avatar) {
        String basisUrl = MinIOUtil.getBasisUrl();
        if (basisUrl != null && avatar.startsWith(basisUrl)) {
            return avatar.substring(basisUrl.length());
        }
        String bucketName = MinIOUtil.getBucketName();
        if (bucketName != null) {
            String marker = "/" + bucketName + "/";
            int markerIndex = avatar.indexOf(marker);
            if (markerIndex >= 0) {
                return avatar.substring(markerIndex + marker.length());
            }
        }
        return avatar;
    }
}
