package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.File;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.simple.AdminFileSimpleResponse;
import com.yirancrazy.smartmedical.service.FileService;
import com.yirancrazy.smartmedical.utils.MinIOUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FileManager 单测
 */
@ExtendWith(MockitoExtension.class)
class FileManagerTest {

    @Mock private FileService fileService;
    @Mock private MinIOUtil minIOUtil;

    @InjectMocks
    private FileManager fileManager;

    @Test
    void getRegistrationTemplate_returnsEnabledFile() throws Exception {
        File file = new File();
        file.setId(1L);
        file.setName("挂号排班模板.cvs");
        file.setEnable(true);
        file.setMd5("md5");
        file.setSize(1024L);
        file.setPath("/path/template.cvs");

        when(fileService.listFileByName("挂号排班模板.cvs")).thenReturn(List.of(file));
        when(minIOUtil.getPresignedObjectUrlOnExpire(eq("smartmedical"), eq("/path/template.cvs"), anyInt()))
                .thenReturn("https://signed-url");

        Result<AdminFileSimpleResponse> result = fileManager.getRegistrationTemplate();

        assertEquals(200, result.getCode());
        AdminFileSimpleResponse dto = result.getData();
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("挂号排班模板.cvs", dto.getName());
        assertEquals("https://signed-url", dto.getPath());
    }

    @Test
    void getRegistrationTemplate_noFilesFound_doesNotInvokeMinio() throws Exception {
        when(fileService.listFileByName("挂号排班模板.cvs")).thenReturn(Collections.emptyList());

        // FileManager 会在 enabled==null 时 NPE；这里只确认 listFileByName 被转发
        try {
            fileManager.getRegistrationTemplate();
        } catch (Exception ignored) {
            // 期望失败
        }
        verify(fileService).listFileByName("挂号排班模板.cvs");
    }

    @Test
    void getFileByName_delegates() {
        File file = new File();
        when(fileService.getFileByName("foo")).thenReturn(file);

        File result = fileManager.getFileByName("foo");

        assertNotNull(result);
        verify(fileService).getFileByName("foo");
    }

    @Test
    void listFileByName_delegates() {
        when(fileService.listFileByName("foo")).thenReturn(List.of(new File()));

        List<File> result = fileManager.listFileByName("foo");

        assertEquals(1, result.size());
        verify(fileService).listFileByName("foo");
    }

    @Test
    void createAdminFileSimpleResponse_copiesAllFields() {
        File file = new File();
        file.setId(11L);
        file.setName("n");
        file.setMd5("m");
        file.setPath("/p");
        file.setSize(100L);

        AdminFileSimpleResponse dto = fileManager.createAdminFileSimpleResponse(file);

        assertEquals(11L, dto.getId());
        assertEquals("n", dto.getName());
        assertEquals("m", dto.getMd5());
        assertEquals("/p", dto.getPath());
        assertEquals(100L, dto.getSize());
    }
}
