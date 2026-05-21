package com.zhuji.userorg.dto;

public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String tokenType;
    private Long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long userId, String username, String tokenType, Long expiresIn) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private Long userId;
        private String username;
        private String tokenType;
        private Long expiresIn;

        public Builder token(String token) {
            this.token = token;
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

        public LoginResponse build() {
            return new LoginResponse(token, userId, username, tokenType, expiresIn);
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}