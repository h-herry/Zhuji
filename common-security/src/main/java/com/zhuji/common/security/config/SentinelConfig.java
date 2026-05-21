package com.zhuji.common.security.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.zhuji.common.core.result.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelConfig {

    private static final Logger log = LoggerFactory.getLogger(SentinelConfig.class);

    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
    }

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule userRule = new FlowRule();
        userRule.setResource("/api/v1/users");
        userRule.setCount(100);
        userRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userRule.setLimitApp("default");
        rules.add(userRule);

        FlowRule orgRule = new FlowRule();
        orgRule.setResource("/api/v1/orgs");
        orgRule.setCount(100);
        orgRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orgRule.setLimitApp("default");
        rules.add(orgRule);

        FlowRule authRule = new FlowRule();
        authRule.setResource("/api/v1/auth/login");
        authRule.setCount(50);
        authRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        authRule.setLimitApp("default");
        rules.add(authRule);

        FlowRuleManager.loadRules(rules);
        log.info("Sentinel流控规则初始化完成");
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        DegradeRule userDegradeRule = new DegradeRule();
        userDegradeRule.setResource("/api/v1/users");
        userDegradeRule.setCount(0.5);
        userDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        userDegradeRule.setTimeWindow(10);
        userDegradeRule.setMinRequestAmount(100);
        userDegradeRule.setStatIntervalMs(10000);
        rules.add(userDegradeRule);

        DegradeRule orgDegradeRule = new DegradeRule();
        orgDegradeRule.setResource("/api/v1/orgs");
        orgDegradeRule.setCount(0.5);
        orgDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        orgDegradeRule.setTimeWindow(10);
        orgDegradeRule.setMinRequestAmount(100);
        orgDegradeRule.setStatIntervalMs(10000);
        rules.add(orgDegradeRule);

        DegradeRuleManager.loadRules(rules);
        log.info("Sentinel熔断规则初始化完成");
    }

    @Bean
    public BlockExceptionHandler blockExceptionHandler() {
        return (HttpServletRequest request, HttpServletResponse response, BlockException e) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

            ApiResponse<Void> apiResponse;
            if (e instanceof com.alibaba.csp.sentinel.slots.block.flow.FlowException) {
                apiResponse = ApiResponse.error(429, "请求过于频繁，请稍后重试");
            } else if (e instanceof com.alibaba.csp.sentinel.slots.block.degrade.DegradeException) {
                apiResponse = ApiResponse.error(503, "服务熔断，请稍后重试");
            } else {
                apiResponse = ApiResponse.error(503, "服务限流，请稍后重试");
            }

            ObjectMapper mapper = new ObjectMapper();
            response.getWriter().write(mapper.writeValueAsString(apiResponse));
            log.warn("Sentinel限流/熔断触发, resource: {}, exception: {}", e.getRule().getResource(), e.getClass().getSimpleName());
        };
    }
}