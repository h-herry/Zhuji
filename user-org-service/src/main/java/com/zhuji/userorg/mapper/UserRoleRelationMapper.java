package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.UserRoleRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleRelationMapper extends BaseMapper<UserRoleRelation> {

    @Select("SELECT * FROM sys_user_role_relation WHERE user_id = #{userId} AND deleted = 0")
    List<UserRoleRelation> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_user_role_relation WHERE role_id = #{roleId} AND deleted = 0")
    List<UserRoleRelation> selectByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT * FROM sys_user_role_relation WHERE user_id = #{userId} AND is_primary = '1' AND deleted = 0 LIMIT 1")
    UserRoleRelation selectPrimaryByUserId(@Param("userId") Long userId);

    @Select("SELECT role_id FROM sys_user_role_relation WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    void batchInsert(@Param("list") List<UserRoleRelation> list);

    void updatePrimaryByUserId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
