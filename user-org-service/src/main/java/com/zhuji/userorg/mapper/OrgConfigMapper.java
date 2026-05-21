package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.OrgConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrgConfigMapper extends BaseMapper<OrgConfig> {

    @Select("SELECT * FROM sys_org_config WHERE org_id = #{orgId} AND deleted = 0")
    List<OrgConfig> selectByOrgId(@Param("orgId") Long orgId);

    @Select("SELECT * FROM sys_org_config WHERE org_id = #{orgId} AND config_type = #{configType} AND deleted = 0")
    List<OrgConfig> selectByOrgIdAndType(@Param("orgId") Long orgId, @Param("configType") String configType);

    @Select("SELECT * FROM sys_org_config WHERE org_id = #{orgId} AND config_key = #{configKey} AND deleted = 0 LIMIT 1")
    OrgConfig selectByOrgIdAndKey(@Param("orgId") Long orgId, @Param("configKey") String configKey);
}