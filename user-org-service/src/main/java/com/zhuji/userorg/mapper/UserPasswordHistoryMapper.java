package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.UserPasswordHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPasswordHistoryMapper extends BaseMapper<UserPasswordHistory> {

    List<UserPasswordHistory> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
