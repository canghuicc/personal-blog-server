package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blog.web.mapper")
public class PersonalBlogServer1Application {

    public static void main(String[] args) {
        SpringApplication.run(PersonalBlogServer1Application.class, args);
    }

}
