package com.zhuji.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.workflow.entity.ProcessTask;

public interface ProcessTaskService {

    Page<ProcessTask> listTodoTasks(Page<ProcessTask> page, String assignee);

    Page<ProcessTask> listDoneTasks(Page<ProcessTask> page, String assignee);

    Page<ProcessTask> listByInstanceId(Page<ProcessTask> page, Long instanceId);

    ProcessTask getById(Long id);
}