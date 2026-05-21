package com.zhuji.common.security.model;

import java.util.Set;

public class LoginUser {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private Set<String> roles;
    private Set<String> permissions;

    public LoginUser() {
    }

    public LoginUser(Long userId, String username, String email, String phone, Set<String> roles, Set<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.roles = roles;
        this.permissions = permissions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private String username;
        private String email;
        private String phone;
        private Set<String> roles;
        private Set<String> permissions;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder permissions(Set<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public LoginUser build() {
            return new LoginUser(userId, username, email, phone, roles, permissions);
        }
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
}