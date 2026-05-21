package com.zhuji.userorg.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private Integer status;
    private Long orgId;
    private LocalDateTime passwordUpdateTime;
    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    public User() {
    }

    public User(Long id, String username, String password, String email, String phone, Integer status, Long orgId, LocalDateTime passwordUpdateTime, LocalDateTime lastLoginTime, LocalDateTime createTime, LocalDateTime updateTime, Integer deleted) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.orgId = orgId;
        this.passwordUpdateTime = passwordUpdateTime;
        this.lastLoginTime = lastLoginTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.deleted = deleted;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String password;
        private String email;
        private String phone;
        private Integer status;
        private Long orgId;
        private LocalDateTime passwordUpdateTime;
        private LocalDateTime lastLoginTime;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private Integer deleted;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
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

        public Builder passwordUpdateTime(LocalDateTime passwordUpdateTime) {
            this.passwordUpdateTime = passwordUpdateTime;
            return this;
        }

        public Builder lastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder deleted(Integer deleted) {
            this.deleted = deleted;
            return this;
        }

        public User build() {
            return new User(id, username, password, email, phone, status, orgId, passwordUpdateTime, lastLoginTime, createTime, updateTime, deleted);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LocalDateTime getPasswordUpdateTime() {
        return passwordUpdateTime;
    }

    public void setPasswordUpdateTime(LocalDateTime passwordUpdateTime) {
        this.passwordUpdateTime = passwordUpdateTime;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
