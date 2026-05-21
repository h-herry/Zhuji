package com.zhuji.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.enums.ErrorCode;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.workflow.entity.ProcessDefinition;
import com.zhuji.workflow.entity.ProcessInstance;
import com.zhuji.workflow.entity.ProcessTask;
import com.zhuji.workflow.mapper.ProcessInstanceMapper;
import com.zhuji.workflow.mapper.ProcessTaskMapper;
import com.zhuji.workflow.service.ProcessDefinitionService;
import com.zhuji.workflow.service.ProcessInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceServiceImpl.class);

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Override
    public Page<ProcessInstance> list(Page<ProcessInstance> page, String processCode, String businessKey, Integer status) {
        LambdaQueryWrapper<ProcessInstance> wrapper = new LambdaQueryWrapper<>();
        if (processCode != null && !processCode.isEmpty()) {
            wrapper.eq(ProcessInstance::getProcessCode, processCode);
        }
        if (businessKey != null && !businessKey.isEmpty()) {
            wrapper.eq(ProcessInstance::getBusinessKey, businessKey);
        }
        if (status != null) {
            wrapper.eq(ProcessInstance::getStatus, status);
        }
        wrapper.orderByDesc(ProcessInstance::getCreateTime);
        return processInstanceMapper.selectPage(page, wrapper);
    }

    @Override
    public ProcessInstance getById(Long id) {
        return processInstanceMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstance startProcess(String processCode, String businessKey, String businessType, String startUserId, String startUserName) {
        ProcessDefinition processDefinition = processDefinitionService.getByCode(processCode);
        if (processDefinition == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.not.found"));
        }
        if (processDefinition.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.not.published"));
        }

        ProcessInstance instance = ProcessInstance.builder()
                .processDefinitionId(processDefinition.getId())
                .processCode(processCode)
                .businessKey(businessKey)
                .businessType(businessType)
                .status(1)
                .currentNodeCode("START")
                .currentNodeName(I18nMessageUtil.getMessage("workflow.node.start"))
                .startUserId(startUserId)
                .startUserName(startUserName)
                .startTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        processInstanceMapper.insert(instance);

        createTask(instance.getId(), processCode, businessKey, "START", I18nMessageUtil.getMessage("workflow.node.start"), startUserId, startUserName);

        log.info("启动流程实例成功: instanceId={}, processCode={}", instance.getId(), processCode);
        return instance;
    }

    private void createTask(Long instanceId, String processCode, String businessKey, String nodeCode, String nodeName, String assignee, String assigneeName) {
        ProcessTask task = ProcessTask.builder()
                .processInstanceId(instanceId)
                .processCode(processCode)
                .businessKey(businessKey)
                .taskCode(UUID.randomUUID().toString())
                .taskName(nodeName + " - " + I18nMessageUtil.getMessage("workflow.task"))
                .nodeCode(nodeCode)
                .nodeName(nodeName)
                .assignee(assignee)
                .assigneeName(assigneeName)
                .status(1)
                .startTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        processTaskMapper.insert(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId, String action, String comment) {
        ProcessTask task = processTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.task.not.found"));
        }
        if (task.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.task.already.processed"));
        }

        task.setStatus(2);
        task.setAction(action);
        task.setComment(comment);
        task.setEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        processTaskMapper.updateById(task);

        ProcessInstance instance = processInstanceMapper.selectById(task.getProcessInstanceId());
        if ("APPROVE".equals(action)) {
            instance.setCurrentNodeCode("END");
            instance.setCurrentNodeName(I18nMessageUtil.getMessage("workflow.node.end"));
            instance.setStatus(2);
            instance.setEndTime(LocalDateTime.now());
            log.info("流程实例完成: instanceId={}", instance.getId());
        } else if ("REJECT".equals(action)) {
            instance.setStatus(3);
            instance.setEndTime(LocalDateTime.now());
            log.info("流程实例被驳回: instanceId={}", instance.getId());
        }
        instance.setUpdateTime(LocalDateTime.now());
        processInstanceMapper.updateById(instance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopProcess(Long instanceId) {
        ProcessInstance instance = processInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.instance.not.found"));
        }
        if (instance.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.instance.not.running"));
        }

        instance.setStatus(4);
        instance.setEndTime(LocalDateTime.now());
        instance.setUpdateTime(LocalDateTime.now());
        processInstanceMapper.updateById(instance);

        LambdaQueryWrapper<ProcessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessTask::getProcessInstanceId, instanceId)
                .eq(ProcessTask::getStatus, 1);
        ProcessTask task = new ProcessTask();
        task.setStatus(4);
        task.setEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        processTaskMapper.update(task, wrapper);

        log.info("终止流程实例成功: instanceId={}", instanceId);
    }
}