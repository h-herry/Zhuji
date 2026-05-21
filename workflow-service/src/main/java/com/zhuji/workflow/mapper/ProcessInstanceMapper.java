package com.zhuji.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.workflow.entity.ProcessInstance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProcessInstanceMapper extends BaseMapper<ProcessInstance> {
}