package com.zhuji.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.file.entity.FileEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<FileEntity> {
}