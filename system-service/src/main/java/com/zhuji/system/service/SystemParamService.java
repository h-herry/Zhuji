package com.zhuji.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.system.dto.SystemParamDTO;
import com.zhuji.system.entity.SystemParam;

import java.util.List;
import java.util.Map;

public interface SystemParamService {

    Page<SystemParam> page(int pageNum, int pageSize, String keyword, Long categoryId);

    List<SystemParam> listAll();

    SystemParam getById(Long id);

    SystemParam getByKey(String paramKey);

    SystemParam create(SystemParamDTO dto);

    SystemParam update(Long id, SystemParamDTO dto);

    void delete(Long id);

    boolean existsByKey(String paramKey);

    String getValue(String paramKey);

    String getValue(String paramKey, String defaultValue);

    Map<String, String> getParamsByCategory(Long categoryId);
}