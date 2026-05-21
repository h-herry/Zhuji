package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.UserOrgRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserOrgRelationMapper extends BaseMapper<UserOrgRelation> {

    @Select("SELECT * FROM sys_user_org_relation WHERE user_id = #{userId} AND deleted = 0")
    List<UserOrgRelation> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_user_org_relation WHERE org_id = #{orgId} AND deleted = 0")
    List<UserOrgRelation> selectByOrgId(@Param("orgId") Long orgId);

    @Select("SELECT * FROM sys_user_org_relation WHERE user_id = #{userId} AND is_primary = '1' AND deleted = 0 LIMIT 1")
    UserOrgRelation selectPrimaryByUserId(@Param("userId") Long userId);

    @Select("SELECT org_id FROM sys_user_org_relation WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectOrgIdsByUserId(@Param("userId") Long userId);
}