package com.zhuji.userorg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = {"com.zhuji"})
@MapperScan("com.zhuji.userorg.mapper")
@EnableCaching
public class UserOrgServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserOrgServiceApplication.class, args);
    }
}