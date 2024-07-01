package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 苍晖
 * @since 2024/7/1 下午12:01
 */

@SpringBootApplication
@MapperScan("com.blog.web.mapper")
public class PersonBlogServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonBlogServerApplication.class, args);
    }
}
