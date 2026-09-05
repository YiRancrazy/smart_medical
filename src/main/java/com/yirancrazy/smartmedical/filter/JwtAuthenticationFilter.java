package com.yirancrazy.smartmedical.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirancrazy.smartmedical.config.SecurityConfig;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * JWT 认证过滤器
 * @Author: YiRanCrazy@gmail.com
 * @Description: 从 Authorization 头读取 Bearer token → 校验 JWT 签名 + Redis 中是否仍存在；
 *              通过后将主体写入 SecurityContext；失败统一返回 401 JSON。
 * @Datetime: 2026-02-02 12:50
 * @Version: 1.0
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.accessSecretKey}")
    private String accessSecretKey;

    @Value("${jwt.accessTokenPrefix:admin-access-token}")
    private String accessTokenPrefix;

    private final RedisUtil redisUtil;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * CROSS-04: 白名单统一由 SecurityConfig.PERMIT_ALL_PATHS 控制，避免 Filter 与 SecurityFilterChain 两份清单不一致
     */
    private static final List<String> PERMIT_ALL_PATHS = List.of(SecurityConfig.PERMIT_ALL_PATHS);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String token = null;

        // 获取token
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            token = header.substring(BEARER_PREFIX.length()).trim();
        }

        // token为空时，返回401
        if (token == null || token.isEmpty()) {
            unauthorized(response, "缺少 access_token");
            return;
        }


        // 验证token
        try {
            // 先验签后解析——parseToken 仅解码 base64 头部，未做签名校验
            JWT jwt = JWTUtil.parseToken(token);
            jwt.setKey(accessSecretKey.getBytes());

            if (!jwt.verify()) {
                unauthorized(response, "无效的 access_token");
                return;
            }

            // 安全解析 payload（此时签名已验证通过）
            JWTPayload payload = jwt.getPayload();
            String sub = String.valueOf(payload.getClaim("sub"));
            long exp = Long.parseLong(String.valueOf(payload.getClaim("exp")));

            // CROSS-02: exp 使用秒级 Unix 时间戳，与 JWT 标准保持一致
            if (exp < System.currentTimeMillis() / 1000) {
                unauthorized(response, "access_token 过期");
                return;
            }

            // 解析 accountId（JWT sub）与 userId claim；login 时以 accountId 为 Redis key 存 token
            Long accountId;
            Long currentUserId;
            try {
                accountId = Long.parseLong(sub);
                Object userIdClaim = payload.getClaim("userId");
                currentUserId = userIdClaim != null ? Long.parseLong(String.valueOf(userIdClaim)) : accountId;
            } catch (NumberFormatException e) {
                unauthorized(response, "无效的 access_token 身份标识");
                return;
            }

            // 校验 Redis 中 token 仍存在（支持 logout 撤销）：login 写入 accessTokenPrefix+accountId，
            // logout 删除同一 key，Filter 必须比对否则登出后旧 token 在 exp 前仍有效
            // BUG-B10: Redis 不可达时降级为仅校验签名和过期时间，避免全站 500
            Object cached;
            boolean redisAvailable;
            try {
                cached = redisUtil.get(accessTokenPrefix + accountId);
                redisAvailable = true;
            } catch (Exception e) {
                log.warn("[jwt] Redis 校验失败，降级为仅校验签名和过期时间: {}", e.getMessage());
                cached = null;
                redisAvailable = false;
            }
            if (redisAvailable && (cached == null || !token.equals(cached.toString()))) {
                unauthorized(response, "access_token 已失效");
                return;
            }

            // todo 后续自定义一个JwtAuthenticationToken
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(sub, null, resolveAuthorities(payload));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 桥接 JWT 上下文到 controller request attributes,
            // 让 controller 可用 @RequestAttribute("currentUserId"/"currentAccountId"/"currentDoctorId"/"currentPharmacistId") 读取已认证身份,
            // 避免 caller-supplied @RequestParam 伪造他人身份。URL 级 role 守卫已由 SecurityConfig 配置。
            request.setAttribute("currentUserId", currentUserId);
            request.setAttribute("currentAccountId", accountId);
            // L4: currentDoctorId 仅对医生角色(2)设置，其余角色置空，避免用户域接口误取到患者 userId
            boolean isDoctor = resolveRoleId(payload) == 2L;
            if (isDoctor) {
                // B06: 登录时已校验 doctor 表存在 id=userId 的记录，约定 account.userId == doctor.id，
                // 因此 currentDoctorId=currentUserId 安全
                request.setAttribute("currentDoctorId", currentUserId);
            } else {
                request.removeAttribute("currentDoctorId");
            }
            // pharmacist 无独立表，currentPharmacistId 用 userId 作操作者ID
            request.setAttribute("currentPharmacistId", currentUserId);
            log.debug("JWT 认证通过：accountId={}, userId={}, uri={}", accountId, currentUserId, request.getRequestURI());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("[jwt] 解析 token 失败: {}", e.getMessage());
            unauthorized(response, "无效的 access_token 格式");
        }
    }

    /**
     * 将 JWT payload 中的 role_id 映射为 Spring Security 角色权限（ROLE_xxx）
     * 角色 ID 映射以 role 表为准：1=系统管理员 / 2=医生 / 4=患者 / 6=药师
     * @param payload JWT payload
     * @return Spring Security 角色权限列表
     */
    private List<SimpleGrantedAuthority> resolveAuthorities(JWTPayload payload) {
        long roleId = resolveRoleId(payload);
        if (roleId < 0) {
            return Collections.emptyList();
        }
        String authority = switch ((int) roleId) {
            case 1 -> "ROLE_admin";
            case 2 -> "ROLE_doctor";
            case 4 -> "ROLE_user";
            case 6 -> "ROLE_pharmacist";
            default -> null;
        };
        // G10: 未知 roleId 不再兜底为 ROLE_user（会错误授权），改为拒绝所有访问
        if (authority == null) {
            log.warn("[jwt] 未知 roleId={}，拒绝授权", roleId);
            return Collections.emptyList();
        }
        return List.of(new SimpleGrantedAuthority(authority));
    }

    /**
     * 将 JWT payload 中的 role claim 规范化为数字角色 ID（兼容 Long / Integer / String 类型，
     * 与 resolveAuthorities 共用，避免类型比较不一致导致角色误判）
     * @param payload JWT payload
     * @return 角色 ID；claim 缺失或非数字时返回 -1
     */
    private long resolveRoleId(JWTPayload payload) {
        Object roleClaim = payload.getClaim("role");
        if (roleClaim == null) {
            return -1;
        }
        try {
            return Long.parseLong(String.valueOf(roleClaim));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 判断 URI 是否在白名单中
     * @param uri 请求 URI
     * @return 是否在白名单中
     */
    private boolean isWhitelisted(String uri) {
        for (String pattern : PERMIT_ALL_PATHS) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回 401 JSON
     * @param response HttpServletResponse
     * @param message 提示信息
     */
    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(401);
        new ObjectMapper().writeValue(response.getWriter(), Result.fail(401, message));
    }
}