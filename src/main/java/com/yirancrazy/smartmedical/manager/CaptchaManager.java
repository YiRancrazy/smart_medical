package com.yirancrazy.smartmedical.manager;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.utils.CaptchaSupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 *
 * @Author: YiRanCrazy@gmail.com
 * @Description: 滑块验证码编排管理，承载获取 / 校验及设备维度通过标记与连错冷却
 * @Datetime: 2026-10-01 10:00
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class CaptchaManager {

    private final CaptchaService captchaService;
    private final CaptchaSupport captchaSupport;

    /**
     * 获取滑块验证码（图片 + token）
     * @param captchaVO 可空请求体，透传 captchaType 等
     * @return anji 标准 ResponseModel
     */
    public ResponseModel get(CaptchaVO captchaVO) {
        CaptchaVO vo = captchaVO == null ? new CaptchaVO() : captchaVO;
        return captchaService.get(vo);
    }

    /**
     * 校验滑块，成功后标记该设备/IP 已通过；连错满 3 次进入 60s 冷却
     * @param captchaVO 滑块校验参数（含 token、pointJson）
     * @param request 用于识别设备ID与客户端IP
     * @return anji 标准 ResponseModel；repCode=0000 表示校验通过
     */
    public ResponseModel check(CaptchaVO captchaVO, HttpServletRequest request) {
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
