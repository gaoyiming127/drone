package com.drone.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器，负责维护实体上标记了
 * {@code @TableField(fill = ...)} 的 createdAt / updatedAt 字段。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时填充创建时间与更新时间。
     *
     * @param metaObject 待插入的实体元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时填充更新时间。
     *
     * <p>更新对象多来自 selectById，updatedAt 已是非空旧值，strictUpdateFill 会跳过填充，
     * 导致 MyBatis-Plus 把旧值显式写回 updated_at，数据库的 ON UPDATE CURRENT_TIMESTAMP 不生效，
     * 因此这里改为直接赋值。</p>
     *
     * @param metaObject 待更新的实体元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);
    }
}
