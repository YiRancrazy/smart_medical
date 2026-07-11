//package com.yirancrazy.smartmedical.manager;
//
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.extra.qrcode.QrCodeException;
//import cn.hutool.extra.qrcode.QrCodeUtil;
//import cn.hutool.extra.qrcode.QrConfig;
//import com.yirancrazy.smartmedical.annotation.Manager;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//
//import java.io.IOException;
//
//import static com.alibaba.nacos.client.naming.backups.FailoverData.DataType.config;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description:
// * @Datetime: 2026-02-13 15:16
// * @Version: 1.0
// */
//
//@Manager
//@RequiredArgsConstructor
//public class QrCodeManager {
//    private final QrConfig qrConfig;
//
//    /**
//     * 生成二维码到文件
//     * @param content 二维码内容
//     * @param filePath 二维码保存路径
//     */
//    public void getQrCodeByFile(String content, String filePath) {
//        try {
//            QrCodeUtil.generate(content,qrConfig, FileUtil.file(filePath));
//        } catch (QrCodeException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 生成二维码并返回流
//     * @param content 二维码内容
//     * @param response 响应
//     */
//    public void getQrCodeByStream(String content, HttpServletResponse response) {
//        try {
////            QrCodeUtil.generate(content, config, "png", response.getOutputStream());
//            QrCodeUtil.generate(content,qrConfig,"png",response.getOutputStream());
//        } catch (QrCodeException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//}