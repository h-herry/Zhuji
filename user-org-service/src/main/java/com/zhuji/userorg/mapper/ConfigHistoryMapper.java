package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.ConfigHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConfigHistoryMapper extends BaseMapper<ConfigHistory> {

    List<ConfigHistory> selectByConfigId(@Param("configId") Long configId);

    List<ConfigHistory> selectByUserId(@Param("userId") Long userId);
}
