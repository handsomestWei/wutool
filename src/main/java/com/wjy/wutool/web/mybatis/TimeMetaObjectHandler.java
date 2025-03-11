package com.wjy.wutool.web.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * mybatis基于策略自动填充时间类型字段值
 */
@Component
public class TimeMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时的填充策略。作用在以下指定字段，配合@TableField(fill = FieldFill.INSERT)使用
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime", new Date(), metaObject);
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }

    /**
     * 更新时的填充策略。作用在以下指定字段，配合@TableField(fill = FieldFill.INSERT_UPDATE)使用
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}