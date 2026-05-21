package com.zhuji.userorg.vo;

import java.time.LocalDateTime;

public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Integer status;
    private Long orgId;
    private LocalDateTime createTime;

    public UserVO() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private Integer status;
        private Long orgId;
        private LocalDateTime createTime;

        public Builder id(Long id) {
            this.id = id;
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

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder orgId(Long orgId) {
            this.orgId = orgId;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public UserVO build() {
            UserVO vo = new UserVO();
            vo.id = this.id;
            vo.username = this.username;
            vo.email = this.email;
            vo.phone = this.phone;
            vo.status = this.status;
            vo.orgId = this.orgId;
            vo.createTime = this.createTime;
            return vo;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}