package com.example.demo;

import com.example.demo.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ShoppingEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingEndApplication.class, args);
        System.out.println("Hello, Spring Boot!");
        log.info("Hello, Spring Boot with Lombok!");

        // 演示 @Data 的效果
        User user = new User();
        user.setId(1L);
        user.setUsername("张三");
        user.setEmail("zhangsan@example.com");
        log.info("用户信息：{}", user.toString());
        log.info("用户名：{}", user.getUsername());
    }
}
