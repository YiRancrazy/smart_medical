package com.yirancrazy.smartmedical.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirancrazy.smartmedical.config.SecurityConfig;
import com.yirancrazy.smartmedical.constant.type.RoleEnum;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.utils.JwtTokenRevoker;
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
 * @Description: 从 Authorization 头读取 Bearer token → 校验 JWT 签名、过期时间和账号级吊销时间戳；
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

    private final JwtTokenRevoker jwtTokenRevoker;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 白名单统一由 SecurityConfig.PERMIT_ALL_PATHS 控制，避免 Filter 与 SecurityFilterChain 两份清单不一致
     */
    private static final List<String> PERMIT_ALL_PATHS = List.of(SecurityConfig.PERMIT_ALL_PATHS);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 过滤器主逻辑
     * @param request 请求
     * @param response 响应
     * @param filterChain 过滤器链
     * @throws ServletException 抛给过滤器链
     * @throws IOException 抛给过滤器链
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

            // exp 使用秒级 Unix 时间戳，与 JWT 标准保持一致
            if (exp < System.currentTimeMillis() / 1000) {
                unauthorized(response, "access_token 过期");
                return;
            }

            // 解析 accountId（JWT sub）与 userId claim；登录时以 accountId 为 Redis key 存 token
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

            // 账号级吊销：Redis 只保存吊销时间戳，账号在吊销时间点之前签发的 access token 全部失效。
            // access token 本身不再写入 Redis，Redis 不可达时降级为仅校验签名和过期时间，避免全站 500。
            long issuedAtMillis = resolveIssuedAtMillis(payload);  // 解析 token 签发时间（毫秒）
            Long revokedAtMillis; // 账号最近一次吊销时间戳（毫秒）
            try {
                revokedAtMillis = jwtTokenRevoker.findRevokedAtMillis(accountId);
            } catch (NumberFormatException e) {
                log.warn("[jwt] 吊销时间戳格式异常，拒绝该 token: accountId={}, value={}", accountId, e.getMessage());
                unauthorized(response, "access_token 已失效");
                return;
            } catch (Exception e) {
                log.warn("[jwt] Redis 吊销校验失败，降级为仅校验签名和过期时间: {}", e.getMessage());
                revokedAtMillis = null;
            }
            if (revokedAtMillis != null && issuedAtMillis <= revokedAtMillis) {
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
     * 角色 ID 映射以 RoleEnum 为准
     * @param payload JWT payload
     * @return Spring Security 角色权限列表
     */
    private List<SimpleGrantedAuthority> resolveAuthorities(JWTPayload payload) {
        long roleId = resolveRoleId(payload);
        if (roleId < 0) {
            return Collections.emptyList();
        }
        RoleEnum role = RoleEnum.getByCode((int) roleId);
        // G10: 未知 roleId 不再兜底为 ROLE_user（会错误授权），改为拒绝所有访问
        if (role == null || role.getSecurityRole() == null) {
            log.warn("[jwt] 未知 roleId={}，拒绝授权", roleId);
            return Collections.emptyList();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getSecurityRole()));
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
     * 解析 token 签发时间（毫秒）
     * <p>新 token 使用 iatMs；历史 token 回退到秒级 iat，均缺失时返回 0，
     * 保证存在吊销记录时历史 token 不会被放行。</p>
     * @param payload JWT payload
     * @return 签发时间戳（毫秒）
     */
    private long resolveIssuedAtMillis(JWTPayload payload) {
        Object iatMsClaim = payload.getClaim("iatMs");
        if (iatMsClaim != null) {
            return Long.parseLong(String.valueOf(iatMsClaim));
        }
        Object iatClaim = payload.getClaim(JWTPayload.ISSUED_AT);
        if (iatClaim != null) {
            return Long.parseLong(String.valueOf(iatClaim)) * 1000L;
        }
        return 0L;
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
