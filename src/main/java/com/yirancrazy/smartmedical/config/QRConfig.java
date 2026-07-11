//package com.yirancrazy.smartmedical.config;
//
//import cn.hutool.extra.qrcode.QrConfig;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.awt.*;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description:
// * @Datetime: 2026-02-13 15:14
// * @Version: 1.0
// */
//
//@Configuration
//public class QRConfig {
//    //采用JavaConfig的方式显示注入hutool中 生成二维码
//    @Bean
//    public QrConfig qrConfig() {
//        return new QrConfig()
//                .setWidth(300)
//                .setHeight(300)
//                .setMargin(2)
//                .setForeColor(Color.BLACK)
//                .setBackColor(Color.WHITE);
//    }
//}