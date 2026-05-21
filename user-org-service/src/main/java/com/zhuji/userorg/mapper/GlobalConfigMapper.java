package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.GlobalConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GlobalConfigMapper extends BaseMapper<GlobalConfig> {

    @Select("SELECT * FROM sys_global_config WHERE config_type = #{configType} AND deleted = 0 ORDER BY sort_order")
    List<GlobalConfig> selectByType(@Param("configType") String configType);

    @Select("SELECT * FROM sys_global_config WHERE config_key = #{configKey} AND deleted = 0 LIMIT 1")
    GlobalConfig selectByKey(@Param("configKey") String configKey);

    @Select("SELECT * FROM sys_global_config WHERE status = '1' AND deleted = 0 ORDER BY sort_order")
    List<GlobalConfig> selectActiveConfigs();
}