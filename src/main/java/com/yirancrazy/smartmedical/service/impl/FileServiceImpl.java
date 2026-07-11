package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yirancrazy.smartmedical.mapper.FileMapper;
import com.yirancrazy.smartmedical.pojo.File;
import com.yirancrazy.smartmedical.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 文件服务实现类
 * @Datetime: 2026-03-06
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;

    @Override
    public Integer insertFile(File file) {
        return fileMapper.insert(file);
    }

    @Override
    public File getFileById(Long id) {
        return fileMapper.selectById(id);
    }

    @Override
    public File getFileByName(String name) {
        return fileMapper.selectOne(new LambdaQueryWrapper<File>()
                .eq(File::getName,name));
    }

    @Override
    public Integer updateFileById(File file) {
        return fileMapper.updateById(file);
    }

    @Override
    public Integer deleteFileById(Long id) {
        return fileMapper.deleteById(id);
    }

    @Override
    public List<File> listAllFiles() {
        return fileMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public Integer deleteFileByIds(List<Long> ids) {
        return fileMapper.deleteByIds(ids);
    }

    @Override
    public List<File> listFileByName(String name) {
        return fileMapper.selectList(new LambdaQueryWrapper<File>().eq(File::getName,name));
    }
}
