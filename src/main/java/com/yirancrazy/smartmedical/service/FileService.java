package com.yirancrazy.smartmedical.service;

import com.yirancrazy.smartmedical.pojo.File;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 文件服务接口
 * @Datetime: 2026-03-06
 * @Version: 1.0
 */

public interface FileService {

    /**
     * 插入文件
     * @param file 文件信息
     * @return 插入结果
     */
    Integer insertFile(File file);

    /**
     * 根据ID查询文件
     * @param id 文件ID
     * @return 文件信息
     */
    File getFileById(Long id);

    /**
     * 根据名称查询文件
     * @param name 文件名称
     * @return 文件信息
     */
    File getFileByName(String name);

    /**
     * 根据ID更新文件
     * @param file 文件信息
     * @return 更新结果
     */
    Integer updateFileById(File file);

    /**
     * 根据ID删除文件
     * @param id 文件ID
     * @return 删除结果
     */
    Integer deleteFileById(Long id);

    /**
     * 查询所有文件
     * @return 文件列表
     */
    List<File> listAllFiles();

    /**
     * 根据ID列表批量删除文件
     * @param ids 文件ID列表
     * @return 删除结果
     */
    Integer deleteFileByIds(List<Long> ids);

    /**
     * 根据名称模糊查询文件
     * @param name 文件名称
     * @return 文件列表
     */
    List<File> listFileByName(String name);
}
