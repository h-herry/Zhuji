package com.zhuji.userorg.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 配置类
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("用户组织服务 API")
                        .version("1.0.0")
                        .description("用户组织服务接口文档")
                        .contact(new Contact().name("Zhuji Team").email("admin@zhuji.com"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")));
    }
}
