package com.zhuji.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
@ConfigurationProperties(prefix = "zhuji")
public class ZhujiProperties {

    private Security security = new Security();
    private Cache cache = new Cache();
    private RateLimit rateLimit = new RateLimit();
    private Logging logging = new Logging();

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Logging getLogging() {
        return logging;
    }

    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public static class Security {
        private String jwtSecret = "zhuji-jwt-secret-key";
        private Long jwtExpire = 86400000L;
        private String[] permitUrls = {"/api/v1/auth/login", "/doc.html", "/swagger-ui/**", "/v3/api-docs/**"};

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public Long getJwtExpire() {
            return jwtExpire;
        }

        public void setJwtExpire(Long jwtExpire) {
            this.jwtExpire = jwtExpire;
        }

        public String[] getPermitUrls() {
            return permitUrls;
        }

        public void setPermitUrls(String[] permitUrls) {
            this.permitUrls = permitUrls;
        }
    }

    public static class Cache {
        private String type = "redis";
        private Long defaultExpire = 3600L;
        private Boolean enabled = true;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getDefaultExpire() {
            return defaultExpire;
        }

        public void setDefaultExpire(Long defaultExpire) {
            this.defaultExpire = defaultExpire;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class RateLimit {
        private Boolean enabled = false;
        private Integer maxRequests = 100;
        private Long windowSeconds = 60L;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(Integer maxRequests) {
            this.maxRequests = maxRequests;
        }

        public Long getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(Long windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }

    public static class Logging {
        private String level = "INFO";
        private Boolean consoleEnabled = true;
        private Boolean fileEnabled = true;

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public Boolean getConsoleEnabled() {
            return consoleEnabled;
        }

        public void setConsoleEnabled(Boolean consoleEnabled) {
            this.consoleEnabled = consoleEnabled;
        }

        public Boolean getFileEnabled() {
            return fileEnabled;
        }

        public void setFileEnabled(Boolean fileEnabled) {
            this.fileEnabled = fileEnabled;
        }
    }
}