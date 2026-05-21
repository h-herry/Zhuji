package com.zhuji.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.workflow.entity.ProcessTask;
import com.zhuji.workflow.mapper.ProcessTaskMapper;
import com.zhuji.workflow.service.ProcessTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {

    private static final Logger log = LoggerFactory.getLogger(ProcessTaskServiceImpl.class);

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Override
    public Page<ProcessTask> listTodoTasks(Page<ProcessTask> page, String assignee) {
        LambdaQueryWrapper<ProcessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessTask::getAssignee, assignee)
                .eq(ProcessTask::getStatus, 1)
                .orderByDesc(ProcessTask::getCreateTime);
        return processTaskMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<ProcessTask> listDoneTasks(Page<ProcessTask> page, String assignee) {
        LambdaQueryWrapper<ProcessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessTask::getAssignee, assignee)
                .in(ProcessTask::getStatus, 2, 3, 4)
                .orderByDesc(ProcessTask::getEndTime);
        return processTaskMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<ProcessTask> listByInstanceId(Page<ProcessTask> page, Long instanceId) {
        LambdaQueryWrapper<ProcessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessTask::getProcessInstanceId, instanceId)
                .orderByAsc(ProcessTask::getCreateTime);
        return processTaskMapper.selectPage(page, wrapper);
    }

    @Override
    public ProcessTask getById(Long id) {
        return processTaskMapper.selectById(id);
    }
}