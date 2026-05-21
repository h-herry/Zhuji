package com.zhuji.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.workflow.entity.ProcessDefinition;

public interface ProcessDefinitionService {

    Page<ProcessDefinition> list(Page<ProcessDefinition> page, String processName);

    ProcessDefinition getById(Long id);

    ProcessDefinition getByCode(String processCode);

    void create(ProcessDefinition processDefinition);

    void update(ProcessDefinition processDefinition);

    void delete(Long id);

    void publish(Long id);

    void offLine(Long id);
}