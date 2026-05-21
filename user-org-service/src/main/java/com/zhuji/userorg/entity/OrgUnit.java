package com.zhuji.userorg.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("sys_org_unit")
public class OrgUnit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orgCode;
    private String fullName;
    private String shortName;
    private Integer orgType;
    private Long parentId;
    private Integer level;
    private String path;
    private Long leaderId;
    private String leaderName;
    private String areaCode;
    private String costCenter;
    private Integer sort;
    private Integer status;
    private Integer isVirtual;
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    public OrgUnit() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String orgCode;
        private String fullName;
        private String shortName;
        private Integer orgType;
        private Long parentId;
        private Integer level;
        private String path;
        private Long leaderId;
        private String leaderName;
        private String areaCode;
        private String costCenter;
        private Integer sort;
        private Integer status;
        private Integer isVirtual;
        private String description;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private Integer deleted;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orgCode(String orgCode) {
            this.orgCode = orgCode;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public Builder orgType(Integer orgType) {
            this.orgType = orgType;
            return this;
        }

        public Builder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder level(Integer level) {
            this.level = level;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder leaderId(Long leaderId) {
            this.leaderId = leaderId;
            return this;
        }

        public Builder leaderName(String leaderName) {
            this.leaderName = leaderName;
            return this;
        }

        public Builder areaCode(String areaCode) {
            this.areaCode = areaCode;
            return this;
        }

        public Builder costCenter(String costCenter) {
            this.costCenter = costCenter;
            return this;
        }

        public Builder sort(Integer sort) {
            this.sort = sort;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder isVirtual(Integer isVirtual) {
            this.isVirtual = isVirtual;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
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

        public OrgUnit build() {
            OrgUnit unit = new OrgUnit();
            unit.id = this.id;
            unit.orgCode = this.orgCode;
            unit.fullName = this.fullName;
            unit.shortName = this.shortName;
            unit.orgType = this.orgType;
            unit.parentId = this.parentId;
            unit.level = this.level;
            unit.path = this.path;
            unit.leaderId = this.leaderId;
            unit.leaderName = this.leaderName;
            unit.areaCode = this.areaCode;
            unit.costCenter = this.costCenter;
            unit.sort = this.sort;
            unit.status = this.status;
            unit.isVirtual = this.isVirtual;
            unit.description = this.description;
            unit.createTime = this.createTime;
            unit.updateTime = this.updateTime;
            unit.deleted = this.deleted;
            return unit;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getOrgType() {
        return orgType;
    }

    public void setOrgType(Integer orgType) {
        this.orgType = orgType;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsVirtual() {
        return isVirtual;
    }

    public void setIsVirtual(Integer isVirtual) {
        this.isVirtual = isVirtual;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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