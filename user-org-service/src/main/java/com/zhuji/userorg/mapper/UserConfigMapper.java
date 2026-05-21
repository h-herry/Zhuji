package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.UserConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserConfigMapper extends BaseMapper<UserConfig> {

    @Select("SELECT * FROM sys_user_config WHERE user_id = #{userId} AND deleted = 0")
    List<UserConfig> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_user_config WHERE user_id = #{userId} AND config_type = #{configType} AND deleted = 0")
    List<UserConfig> selectByUserIdAndType(@Param("userId") Long userId, @Param("configType") String configType);

    @Select("SELECT * FROM sys_user_config WHERE user_id = #{userId} AND config_key = #{configKey} AND deleted = 0 LIMIT 1")
    UserConfig selectByUserIdAndKey(@Param("userId") Long userId, @Param("configKey") String configKey);
}