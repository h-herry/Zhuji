package com.zhuji.system.service;

import com.zhuji.system.dto.ParamCategoryDTO;
import com.zhuji.system.entity.ParamCategory;

import java.util.List;

public interface ParamCategoryService {

    List<ParamCategory> listAll();

    ParamCategory getById(Long id);

    ParamCategory create(ParamCategoryDTO dto);

    ParamCategory update(Long id, ParamCategoryDTO dto);

    void delete(Long id);

    boolean existsByCode(String code);
}