package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.FilePath;
import com.yirancrazy.smartmedical.pojo.File;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.simple.AdminFileSimpleResponse;
import com.yirancrazy.smartmedical.service.FileService;
import com.yirancrazy.smartmedical.utils.MinIOUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 文件管理器
 * @Datetime: 2026-03-18 16:03
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class FileManager {
    private final FileService fileService;
    private final MinIOUtil minIOUtil;

    /**
     * 获取挂号排班模板
     * @return 文件对象
     */
    public Result<AdminFileSimpleResponse> getRegistrationTemplate() {
        List<File> files = this.listFileByName("挂号排班模板.cvs");
        File file = files.stream().filter(f->f.getEnable().equals(true)).findFirst().orElse(null);


        // 通过file 中的真实 path 生成临时访问链接，时效12小时
        String url = "";
        try {
            url = minIOUtil.getPresignedObjectUrlOnExpire(FilePath.SMART_MEDICAL_MINIO_BUCKET_NAME,file.getPath(), 12);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        file.setPath(url);


        return Result.success(createAdminFileSimpleResponse(file));
    };

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
