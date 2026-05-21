package com.zhuji.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.zhuji")
@EnableFeignClients(basePackages = "com.zhuji.thirdparty.feign")
public class ThirdPartyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThirdPartyServiceApplication.class, args);
    }
}