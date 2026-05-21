package com.zhuji.userorg.dto;

public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String tokenType;
    private Long expiresIn;
    private String passwordExpiryWarning;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, Long userId, String username,
                       String tokenType, Long expiresIn, String passwordExpiryWarning) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.passwordExpiryWarning = passwordExpiryWarning;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String username;
        private String tokenType;
        private Long expiresIn;
        private String passwordExpiryWarning;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder passwordExpiryWarning(String passwordExpiryWarning) {
            this.passwordExpiryWarning = passwordExpiryWarning;
            return this;
        }

        public LoginResponse build() {
            return new LoginResponse(accessToken, refreshToken, userId, username, tokenType, expiresIn, passwordExpiryWarning);
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getPasswordExpiryWarning() {
        return passwordExpiryWarning;
    }

    public void setPasswordExpiryWarning(String passwordExpiryWarning) {
        this.passwordExpiryWarning = passwordExpiryWarning;
    }
}
