package com.zhuji.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.enums.ErrorCode;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.workflow.entity.ProcessDefinition;
import com.zhuji.workflow.mapper.ProcessDefinitionMapper;
import com.zhuji.workflow.service.ProcessDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(ProcessDefinitionServiceImpl.class);

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Override
    public Page<ProcessDefinition> list(Page<ProcessDefinition> page, String processName) {
        LambdaQueryWrapper<ProcessDefinition> wrapper = new LambdaQueryWrapper<>();
        if (processName != null && !processName.isEmpty()) {
            wrapper.like(ProcessDefinition::getProcessName, processName);
        }
        wrapper.orderByDesc(ProcessDefinition::getCreateTime);
        return processDefinitionMapper.selectPage(page, wrapper);
    }

    @Override
    public ProcessDefinition getById(Long id) {
        return processDefinitionMapper.selectById(id);
    }

    @Override
    public ProcessDefinition getByCode(String processCode) {
        LambdaQueryWrapper<ProcessDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessDefinition::getProcessCode, processCode);
        return processDefinitionMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ProcessDefinition processDefinition) {
        ProcessDefinition existing = getByCode(processDefinition.getProcessCode());
        if (existing != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.code.exists"));
        }
        processDefinition.setVersion(1);
        processDefinition.setStatus(0);
        processDefinition.setCreateTime(LocalDateTime.now());
        processDefinition.setUpdateTime(LocalDateTime.now());
        processDefinitionMapper.insert(processDefinition);
        log.info("创建流程定义成功: processCode={}", processDefinition.getProcessCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProcessDefinition processDefinition) {
        ProcessDefinition existing = getById(processDefinition.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.not.found"));
        }
        if (existing.getStatus() == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.cannot.modify"));
        }
        processDefinition.setUpdateTime(LocalDateTime.now());
        processDefinitionMapper.updateById(processDefinition);
        log.info("更新流程定义成功: id={}", processDefinition.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ProcessDefinition existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.not.found"));
        }
        if (existing.getStatus() == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.cannot.delete"));
        }
        processDefinitionMapper.deleteById(id);
        log.info("删除流程定义成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        ProcessDefinition existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.not.found"));
        }
        if (existing.getStatus() == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.already.published"));
        }
        existing.setStatus(1);
        existing.setUpdateTime(LocalDateTime.now());
        processDefinitionMapper.updateById(existing);
        log.info("发布流程定义成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offLine(Long id) {
        ProcessDefinition existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.not.found"));
        }
        if (existing.getStatus() == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), I18nMessageUtil.getMessage("workflow.process.not.published"));
        }
        existing.setStatus(0);
        existing.setUpdateTime(LocalDateTime.now());
        processDefinitionMapper.updateById(existing);
        log.info("下线流程定义成功: id={}", id);
    }
}