package com.zhuji.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.workflow.entity.ProcessInstance;

public interface ProcessInstanceService {

    Page<ProcessInstance> list(Page<ProcessInstance> page, String processCode, String businessKey, Integer status);

    ProcessInstance getById(Long id);

    ProcessInstance startProcess(String processCode, String businessKey, String businessType, String startUserId, String startUserName);

    void completeTask(Long taskId, String action, String comment);

    void stopProcess(Long instanceId);
}