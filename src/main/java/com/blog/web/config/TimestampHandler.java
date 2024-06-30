package com.blog.web.config;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author 苍晖
 * @since 2024/7/1 上午11:48
 *
 */
@Component
public class TimestampHandler {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 格式化当前时间为指定格式的字符串。
     * @return 当前时间的格式化字符串
     */
    public String formatCurrentTime() {
        return LocalDateTime.now().format(formatter);
    }

    /**
     * 在插入数据前处理数据，设置createdAt和updatedAt字段。
     * @param entity 待处理的实体对象
     */
    public void preprocessForInsert(Object entity) {
        preprocess(entity, true, true);
    }

    /**
     * 在更新数据前处理数据，设置updatedAt字段。
     * @param entity 待处理的实体对象
     */
    public void preprocessForUpdate(Object entity) {
        preprocess(entity, false, true);
    }

    /**
     * 对给定的实体对象进行预处理，主要包括设置创建时间和更新时间。
     *
     * @param entity 需要进行预处理的实体对象。
     * @param setCreatedAt 是否设置创建时间。如果为true，则会将当前时间设置为实体的创建时间。
     * @param setUpdatedAt 是否设置更新时间。如果为true，则会将当前时间设置为实体的更新时间。
     */
    private void preprocess(Object entity, boolean setCreatedAt, boolean setUpdatedAt) {
        try {
            // 获取实体类的Class对象
            Class<?> entityClass = entity.getClass();

            // 获取实体类中的createdAt字段
            Field createdAtField = entityClass.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);

            // 如果需要，设置创建时间
            if (setCreatedAt) {
                createdAtField.set(entity, LocalDateTime.now());
            }

            // 检查updatedAt字段是否存在，如果存在则进行设置
            if (setUpdatedAt) {
                Field updatedAtField = entityClass.getDeclaredField("updatedAt");
                updatedAtField.setAccessible(true);
                updatedAtField.set(entity, LocalDateTime.now());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 如果在处理过程中出现异常，则抛出运行时异常
            throw new RuntimeException("获取时间错误", e);
        }
    }

}
