package com.zhuji.common.config.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDiscoveryClient
public class NacosConfig {

    private static final Logger log = LoggerFactory.getLogger(NacosConfig.class);
}