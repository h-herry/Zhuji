package com.zhuji.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.system.dto.ParamCategoryDTO;
import com.zhuji.system.entity.ParamCategory;
import com.zhuji.system.mapper.ParamCategoryMapper;
import com.zhuji.system.service.ParamCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParamCategoryServiceImpl implements ParamCategoryService {

    private final ParamCategoryMapper paramCategoryMapper;

    public ParamCategoryServiceImpl(ParamCategoryMapper paramCategoryMapper) {
        this.paramCategoryMapper = paramCategoryMapper;
    }

    @Override
    public List<ParamCategory> listAll() {
        return paramCategoryMapper.selectList(
            new LambdaQueryWrapper<ParamCategory>()
                .orderByAsc(ParamCategory::getSortOrder)
        );
    }

    @Override
    public ParamCategory getById(Long id) {
        ParamCategory category = paramCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("system.category.not.found"));
        }
        return category;
    }

    @Override
    public ParamCategory create(ParamCategoryDTO dto) {
        if (existsByCode(dto.getCategoryCode())) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("system.category.code.exists", dto.getCategoryCode()));
        }

        ParamCategory category = new ParamCategory();
        BeanUtils.copyProperties(dto, category);

        paramCategoryMapper.insert(category);
        return category;
    }

    @Override
    public ParamCategory update(Long id, ParamCategoryDTO dto) {
        ParamCategory category = getById(id);

        if (!category.getCategoryCode().equals(dto.getCategoryCode()) && existsByCode(dto.getCategoryCode())) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("system.category.code.exists", dto.getCategoryCode()));
        }

        BeanUtils.copyProperties(dto, category);
        paramCategoryMapper.updateById(category);

        return category;
    }

    @Override
    public void delete(Long id) {
        getById(id);
        paramCategoryMapper.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return paramCategoryMapper.exists(
            new LambdaQueryWrapper<ParamCategory>()
                .eq(ParamCategory::getCategoryCode, code)
        );
    }
}