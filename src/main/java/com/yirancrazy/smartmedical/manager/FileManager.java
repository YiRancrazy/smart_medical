package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.File;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.simple.AdminFileSimpleResponse;
import com.yirancrazy.smartmedical.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 文件管理器
 * @Datetime: 2026-03-18 16:03
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class FileManager {
    private final FileService fileService;

    private static final String REGISTRATION_TEMPLATE_DOWNLOAD_URL = "/api/admin/v1/excel/download/registration/template";

    /**
     * 获取挂号排班模板
     * @return 文件对象
     */
    public Result<AdminFileSimpleResponse> getRegistrationTemplate() {
        List<File> files = this.listFileByName("挂号排班模板.csv");
        File file = files.stream().filter(f -> Boolean.TRUE.equals(f.getEnable())).findFirst().orElse(null);

        if (file == null) {
            AdminFileSimpleResponse response = new AdminFileSimpleResponse();
            response.setName("挂号排班导入模板.csv");
            response.setPath(REGISTRATION_TEMPLATE_DOWNLOAD_URL);
            return Result.success(response);
        }

        file.setPath(REGISTRATION_TEMPLATE_DOWNLOAD_URL);
        return Result.success(createAdminFileSimpleResponse(file));
    }

    /**
     * 根据文件名获取文件
     * @param name 文件名
     * @return 文件对象
     */
    public File getFileByName(String name){
        return fileService.getFileByName(name);
    }

    /**
     * 根据文件名列出文件
     * @param name 文件名
     * @return 文件对象
     */
    public List<File> listFileByName(String name){
        return fileService.listFileByName(name);
    }


    public AdminFileSimpleResponse createAdminFileSimpleResponse(File file){
        AdminFileSimpleResponse response = new AdminFileSimpleResponse();
        response.setId(file.getId());
        response.setName(file.getName());
        response.setMd5(file.getMd5());
        response.setPath(file.getPath());
        response.setSize(file.getSize());
        return response;
    }

}
