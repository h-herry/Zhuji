package com.zhuji.userorg.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuji.userorg.entity.OrgType;
import com.zhuji.userorg.mapper.OrgTypeMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgTypeService extends ServiceImpl<OrgTypeMapper, OrgType> {

    @Cacheable(value = "orgType", key = "'all'")
    public List<OrgType> getAllOrgTypes() {
        return lambdaQuery()
                .eq(OrgType::getStatus, 1)
                .orderByAsc(OrgType::getSort)
                .list();
    }

    @Cacheable(value = "orgType", key = "#typeCode")
    public OrgType getByTypeCode(Integer typeCode) {
        return lambdaQuery()
                .eq(OrgType::getTypeCode, typeCode)
                .eq(OrgType::getStatus, 1)
                .one();
    }

    @Cacheable(value = "orgType", key = "#typeKey")
    public OrgType getByTypeKey(String typeKey) {
        return lambdaQuery()
                .eq(OrgType::getTypeKey, typeKey)
                .eq(OrgType::getStatus, 1)
                .one();
    }
}