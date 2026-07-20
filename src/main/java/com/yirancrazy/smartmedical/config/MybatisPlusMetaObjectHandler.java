package com.yirancrazy.smartmedical.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:  MyBatis-Plus 配置 insert 时填 createTime/updateTime/deleted；update 时填 updateTime
 * @Datetime: 2026-02-02 13:20
 * @Version: 1.0
 */

public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_TIME = "updateTime";
    private static final String DELETED = "deleted";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        if (metaObject.hasGetter(CREATE_TIME)) {
            this.fillStrategy(metaObject, CREATE_TIME, now);
        }
        if (metaObject.hasGetter(UPDATE_TIME)) {
            this.fillStrategy(metaObject, UPDATE_TIME, now);
        }
        if (metaObject.hasGetter(DELETED)) {
            this.fillStrategy(metaObject, DELETED, false);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasGetter(UPDATE_TIME)) {
            this.fillStrategy(metaObject, UPDATE_TIME, LocalDateTime.now());
        }
    }
}