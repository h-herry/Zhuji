package com.zhuji.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.workflow.entity.ProcessDefinition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProcessDefinitionMapper extends BaseMapper<ProcessDefinition> {
}