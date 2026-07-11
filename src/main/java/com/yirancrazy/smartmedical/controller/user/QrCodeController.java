//package com.yirancrazy.smartmedical.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import com.yirancrazy.smartmedical.manager.QrCodeManager;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description:
// * @Datetime: 2026-02-13 15:26
// * @Version: 1.0
// */
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/qrcode")
//public class QrCodeController {
//    private final QrCodeManager qrCodeManager;
//
//    @GetMapping()
//    public void createQrCodeByFile(String content, String filePath) {
//        qrCodeManager.getQrCodeByFile(content, filePath);
//    }
//
//}