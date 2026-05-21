package com.zhuji.userorg.vo;

import java.time.LocalDateTime;
import java.util.List;

public class OrgTreeVO {
    private Long id;
    private String orgCode;
    private String fullName;
    private String shortName;
    private Integer orgType;
    private String orgTypeName;
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
    private List<OrgTreeVO> children;

    public OrgTreeVO() {
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
        private String orgTypeName;
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
        private List<OrgTreeVO> children;

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

        public Builder orgTypeName(String orgTypeName) {
            this.orgTypeName = orgTypeName;
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

        public Builder children(List<OrgTreeVO> children) {
            this.children = children;
            return this;
        }

        public OrgTreeVO build() {
            OrgTreeVO vo = new OrgTreeVO();
            vo.id = this.id;
            vo.orgCode = this.orgCode;
            vo.fullName = this.fullName;
            vo.shortName = this.shortName;
            vo.orgType = this.orgType;
            vo.orgTypeName = this.orgTypeName;
            vo.parentId = this.parentId;
            vo.level = this.level;
            vo.path = this.path;
            vo.leaderId = this.leaderId;
            vo.leaderName = this.leaderName;
            vo.areaCode = this.areaCode;
            vo.costCenter = this.costCenter;
            vo.sort = this.sort;
            vo.status = this.status;
            vo.isVirtual = this.isVirtual;
            vo.description = this.description;
            vo.createTime = this.createTime;
            vo.children = this.children;
            return vo;
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

    public String getOrgTypeName() {
        return orgTypeName;
    }

    public void setOrgTypeName(String orgTypeName) {
        this.orgTypeName = orgTypeName;
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

    public List<OrgTreeVO> getChildren() {
        return children;
    }

    public void setChildren(List<OrgTreeVO> children) {
        this.children = children;
    }
}