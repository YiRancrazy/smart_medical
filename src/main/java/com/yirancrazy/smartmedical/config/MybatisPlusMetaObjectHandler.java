//package com.yirancrazy.smartmedical.config;
//
//import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
//import org.apache.ibatis.reflection.MetaObject;
//
//import java.time.LocalDateTime;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description:
// * @Datetime: 2026-02-02 13:20
// * @Version: 1.0
// */
//
//public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {
//    private static final String CREATE_TIME = "createTime";
//    private static final String UPDATE_TIME = "updateTime";
//
//    private static final String IS_DELETED = "isDeleted";
//
//    @Override
//    public void insertFill(MetaObject metaObject) {
//        // 自动填入创建时间
//        if (metaObject.hasGetter(CREATE_TIME)) {
//            this.fillStrategy(metaObject, CREATE_TIME, LocalDateTime.now());
//        }
//
//        // 自动填入更新时间
//        if (metaObject.hasGetter(UPDATE_TIME)) {
//            this.fillStrategy(metaObject, UPDATE_TIME, LocalDateTime.now());
//        }
//
//        if(metaObject.hasGetter(IS_DELETED)){
//            this.fillStrategy(metaObject,IS_DELETED,false);
//        }
//    }
//
//    @Override
//    public void updateFill(MetaObject metaObject) {
//        // 自动填入更新时间
//        if (metaObject.hasGetter(UPDATE_TIME)) {
//            this.fillStrategy(metaObject, UPDATE_TIME, LocalDateTime.now());
//        }
//    }
//}