package com.zhuji.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.system.dto.SystemParamDTO;
import com.zhuji.system.entity.SystemParam;
import com.zhuji.system.mapper.SystemParamMapper;
import com.zhuji.system.service.SystemParamService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemParamServiceImpl implements SystemParamService {

    private final SystemParamMapper systemParamMapper;

    public SystemParamServiceImpl(SystemParamMapper systemParamMapper) {
        this.systemParamMapper = systemParamMapper;
    }

    @Override
    public Page<SystemParam> page(int pageNum, int pageSize, String keyword, Long categoryId) {
        Page<SystemParam> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SystemParam> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SystemParam::getParamKey, keyword)
                    .or()
                    .like(SystemParam::getParamValue, keyword)
                    .or()
                    .like(SystemParam::getDescription, keyword));
        }

        if (categoryId != null) {
            wrapper.eq(SystemParam::getCategoryId, categoryId);
        }

        wrapper.orderByDesc(SystemParam::getCreateTime);
        return systemParamMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SystemParam> listAll() {
        return systemParamMapper.selectList(
            new LambdaQueryWrapper<SystemParam>()
                .orderByDesc(SystemParam::getSortOrder)
        );
    }

    @Override
    public SystemParam getById(Long id) {
        SystemParam param = systemParamMapper.selectById(id);
        if (param == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("system.param.not.found"));
        }
        return param;
    }

    @Override
    public SystemParam getByKey(String paramKey) {
        return systemParamMapper.selectOne(
            new LambdaQueryWrapper<SystemParam>()
                .eq(SystemParam::getParamKey, paramKey)
                .eq(SystemParam::getStatus, "1")
        );
    }

    @Override
    public SystemParam create(SystemParamDTO dto) {
        if (existsByKey(dto.getParamKey())) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("system.param.key.exists", dto.getParamKey()));
        }

        SystemParam param = new SystemParam();
        BeanUtils.copyProperties(dto, param);

        if (param.getStatus() == null) {
            param.setStatus("1");
        }

        systemParamMapper.insert(param);
        return param;
    }

    @Override
    public SystemParam update(Long id, SystemParamDTO dto) {
        SystemParam param = getById(id);

        if (!param.getParamKey().equals(dto.getParamKey()) && existsByKey(dto.getParamKey())) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("system.param.key.exists", dto.getParamKey()));
        }

        BeanUtils.copyProperties(dto, param);
        systemParamMapper.updateById(param);

        return param;
    }

    @Override
    public void delete(Long id) {
        getById(id);
        systemParamMapper.deleteById(id);
    }

    @Override
    public boolean existsByKey(String paramKey) {
        return systemParamMapper.exists(
            new LambdaQueryWrapper<SystemParam>()
                .eq(SystemParam::getParamKey, paramKey)
        );
    }

    @Override
    public String getValue(String paramKey) {
        SystemParam param = getByKey(paramKey);
        if (param == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("system.param.key.not.found", paramKey));
        }
        return param.getParamValue();
    }

    @Override
    public String getValue(String paramKey, String defaultValue) {
        SystemParam param = getByKey(paramKey);
        return param != null ? param.getParamValue() : defaultValue;
    }

    @Override
    public Map<String, String> getParamsByCategory(Long categoryId) {
        List<SystemParam> params = systemParamMapper.selectList(
            new LambdaQueryWrapper<SystemParam>()
                .eq(SystemParam::getCategoryId, categoryId)
                .eq(SystemParam::getStatus, "1")
                .orderByAsc(SystemParam::getSortOrder)
        );

        return params.stream()
                .collect(Collectors.toMap(SystemParam::getParamKey, SystemParam::getParamValue));
    }
}