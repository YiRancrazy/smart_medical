package com.yirancrazy.smartmedical.controller;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.yirancrazy.smartmedical.utils.CaptchaSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 滑块验证码控制器（行为验证码，无需登录即可访问）
 * @Author: YiRanCrazy@gmail.com
 * @Description: get 下发滑块图片与 token；check 独立校验滑块 —— 校验通过后按「设备ID+IP」
 *              写入通过标记，登录端守卫据此放行（登录请求不再携带验证码）。
 * @Datetime: 2026-09-04 14:20
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/captcha")
@RequiredArgsConstructor
@Tag(name = "验证码", description = "行为验证码（滑块）获取与校验")
public class CaptchaControllerV1 {

    private final CaptchaService captchaService;
    private final CaptchaSupport captchaSupport;

    /**
     * 获取滑块验证码（图片 + token）
     * @param captchaVO 可空请求体，透传 captchaType 等
     * @return anji 标准 ResponseModel（repCode=0000 时 repData 含 token / 原图 / 滑块图）
     */
    @PostMapping("/get")
    @Operation(summary = "获取滑块验证码", description = "返回滑块背景图、缺口图与 token，供前端渲染")
    public ResponseModel get(@RequestBody(required = false) CaptchaVO captchaVO) {
        CaptchaVO vo = captchaVO == null ? new CaptchaVO() : captchaVO;
        return captchaService.get(vo);
    }

    /**
     * 校验滑块（独立接口）。校验通过后标记该设备/IP 已通过；连错满 3 次进入 60s 冷却。
     * @param captchaVO 滑块校验参数（含 token、pointJson）
     * @param request 用于识别设备ID与客户端IP
     * @return anji 标准 ResponseModel；repCode=0000 表示校验通过
     */
    @PostMapping("/check")
    @Operation(summary = "校验滑块", description = "独立校验滑块，通过后登录接口将按设备ID/IP放行")
    public ResponseModel check(@RequestBody CaptchaVO captchaVO, HttpServletRequest request) {
        if (captchaSupport.isCooled(request)) {
            return ResponseModel.errorMsg("操作过于频繁，请稍后再试");
        }
        ResponseModel response = captchaService.check(captchaVO);
        if (response.isSuccess()) {
            captchaSupport.markPassed(request);
            captchaSupport.clearFail(request);
        } else if (captchaSupport.recordFail(request)) {
            return ResponseModel.errorMsg("连续校验失败次数过多，已暂停，请稍后再试");
        }
        return response;
    }
}