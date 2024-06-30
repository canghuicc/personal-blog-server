package com.blog.web.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Configuration
public class MyRedisConfig {

    @Autowired
    private RedisConnectionFactory factory;

    /**
     * 创建并配置RedisTemplate，用于操作Redis数据库。
     * 这个方法不接受任何参数，返回配置好的RedisTemplate实例。
     *
     * @return RedisTemplate<String, Object> 配置好的Redis模板，其中键使用String序列化，值使用JSON格式序列化。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory); // 设置Redis连接工厂

        // 设置键的序列化方式为StringRedisSerializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // 配置ObjectMapper以适应JSON序列化和反序列化
        ObjectMapper objectMapper = new ObjectMapper();

        // 配置ObjectMapper的序列化和反序列化设置
        configureObjectMapper(objectMapper);

        // 使用Jackson2JsonRedisSerializer来序列化值
        Jackson2JsonRedisSerializer<Object> redisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        redisTemplate.setValueSerializer(redisSerializer);

        return redisTemplate;
    }

    /**
     * 配置ObjectMapper的各种序列化和反序列化设置。
     * @param objectMapper ObjectMapper实例，用于配置序列化和反序列化行为。
     */
    private void configureObjectMapper(ObjectMapper objectMapper){
        // 设置所有属性可序列化
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 设置日期格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 设置时区为默认时区
        objectMapper.setTimeZone(TimeZone.getDefault());
        // 禁用使用注解
        objectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
        // 反序列化时忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 序列化时忽略空bean
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 启用默认类型推断
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        // 只序列化非null值
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

}
