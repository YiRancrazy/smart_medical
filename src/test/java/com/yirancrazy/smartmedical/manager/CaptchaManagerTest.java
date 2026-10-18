package com.yirancrazy.smartmedical.manager;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.yirancrazy.smartmedical.utils.CaptchaSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CaptchaManager 单测
 * 覆盖：获取透传、冷却拦截、校验通过标记、连错阈值冷却。
 * @Author: YiRanCrazy@gmail.com
 * @Description: CaptchaManager 单测
 * @Datetime: 2026-10-01 10:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class CaptchaManagerTest {

    @Mock private CaptchaService captchaService;
    @Mock private CaptchaSupport captchaSupport;
    @Mock private HttpServletRequest request;

    @InjectMocks
    private CaptchaManager captchaManager;

    @Test
    void get_nullBody_passesEmptyVoToService() {
        ResponseModel expected = ResponseModel.successData("token");
        when(captchaService.get(any(CaptchaVO.class))).thenReturn(expected);

        ResponseModel result = captchaManager.get(null);

        ArgumentCaptor<CaptchaVO> captor = ArgumentCaptor.forClass(CaptchaVO.class);
        verify(captchaService).get(captor.capture());
        assertTrue(captor.getValue() != null);
        assertEquals(expected, result);
    }

    @Test
    void get_withBody_passesSameVoToService() {
        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaType("blockPuzzle");
        ResponseModel expected = ResponseModel.successData("token");
        when(captchaService.get(vo)).thenReturn(expected);

        ResponseModel result = captchaManager.get(vo);

        assertEquals(expected, result);
        verify(captchaService).get(vo);
    }

    @Test
    void check_cooled_returnsErrorAndSkipsService() {
        when(captchaSupport.isCooled(request)).thenReturn(true);

        ResponseModel result = captchaManager.check(new CaptchaVO(), request);

        assertFalse(result.isSuccess());
        assertTrue(result.getRepMsg().contains("操作过于频繁"));
        verify(captchaService, never()).check(any());
        verify(captchaSupport, never()).markPassed(any());
    }

    @Test
    void check_success_marksPassedAndClearsFail() {
        CaptchaVO vo = new CaptchaVO();
        ResponseModel success = ResponseModel.success();
        when(captchaSupport.isCooled(request)).thenReturn(false);
        when(captchaService.check(vo)).thenReturn(success);

        ResponseModel result = captchaManager.check(vo, request);

        assertTrue(result.isSuccess());
        verify(captchaSupport).markPassed(request);
        verify(captchaSupport).clearFail(request);
        verify(captchaSupport, never()).recordFail(request);
    }

    @Test
    void check_failUnderThreshold_returnsOriginalResponse() {
        CaptchaVO vo = new CaptchaVO();
        ResponseModel failure = ResponseModel.errorMsg("校验失败");
        when(captchaSupport.isCooled(request)).thenReturn(false);
        when(captchaService.check(vo)).thenReturn(failure);
        when(captchaSupport.recordFail(request)).thenReturn(false);

        ResponseModel result = captchaManager.check(vo, request);

        assertFalse(result.isSuccess());
        assertEquals("校验失败", result.getRepMsg());
        verify(captchaSupport).recordFail(request);
        verify(captchaSupport, never()).markPassed(any());
        verify(captchaSupport, never()).clearFail(any());
    }

    @Test
    void check_failReachingThreshold_returnsCooldownError() {
        CaptchaVO vo = new CaptchaVO();
        when(captchaSupport.isCooled(request)).thenReturn(false);
        when(captchaService.check(vo)).thenReturn(ResponseModel.errorMsg("校验失败"));
        when(captchaSupport.recordFail(request)).thenReturn(true);

        ResponseModel result = captchaManager.check(vo, request);

        assertFalse(result.isSuccess());
        assertTrue(result.getRepMsg().contains("连续校验失败次数过多"));
        verify(captchaSupport).recordFail(request);
    }
}
