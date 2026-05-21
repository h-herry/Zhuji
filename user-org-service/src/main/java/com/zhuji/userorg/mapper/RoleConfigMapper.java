package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.RoleConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleConfigMapper extends BaseMapper<RoleConfig> {

    @Select("SELECT * FROM sys_role_config WHERE role_id = #{roleId} AND deleted = 0")
    List<RoleConfig> selectByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT * FROM sys_role_config WHERE role_id = #{roleId} AND config_type = #{configType} AND deleted = 0")
    List<RoleConfig> selectByRoleIdAndType(@Param("roleId") Long roleId, @Param("configType") String configType);

    @Select("SELECT * FROM sys_role_config WHERE role_id = #{roleId} AND config_key = #{configKey} AND deleted = 0 LIMIT 1")
    RoleConfig selectByRoleIdAndKey(@Param("roleId") Long roleId, @Param("configKey") String configKey);
}