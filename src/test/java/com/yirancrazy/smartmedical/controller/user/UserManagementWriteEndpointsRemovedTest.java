package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.DepartmentManager;
import com.yirancrazy.smartmedical.manager.MedicineManager;
import com.yirancrazy.smartmedical.manager.OrderStatusLogManager;
import com.yirancrazy.smartmedical.manager.OrderTypeManager;
import com.yirancrazy.smartmedical.manager.ShiftManager;
import com.yirancrazy.smartmedical.manager.UserManager;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户端管理写接口移除回归测试
 * @Author: YiRanCrazy@gmail.com
 * @Description: 校验 P0-3：患者角色可达的 /api/user/v1/** 不再暴露管理写入口，同时查询接口仍可用
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

class UserManagementWriteEndpointsRemovedTest {

    /**
     * 科室写接口（更新 / 删除 / 批量删除）已从用户端移除，仅保留查询
     */
    @Test
    void departmentWriteEndpoints_shouldNotBeMapped() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserDepartmentControllerV1(mock(DepartmentManager.class)))
                .build();

        mockMvc.perform(put("/api/user/v1/department/1")).andExpect(status().is4xxClientError());
        mockMvc.perform(delete("/api/user/v1/department/1")).andExpect(status().is4xxClientError());
        mockMvc.perform(delete("/api/user/v1/department/batch")).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/user/v1/department/1")).andExpect(status().isOk());
    }

    /**
     * 药品新增接口已从用户端移除，仅保留查询
     */
    @Test
    void medicineAddEndpoint_shouldNotBeMapped() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserMedicineControllerV1(mock(MedicineManager.class)))
                .build();

        mockMvc.perform(post("/api/user/v1/medicine/add")).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/user/v1/medicine/1")).andExpect(status().isOk());
    }

    /**
     * 订单类型新增接口已从用户端移除，仅保留查询
     */
    @Test
    void orderTypeAddEndpoint_shouldNotBeMapped() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserOrderTypeControllerV1(mock(OrderTypeManager.class)))
                .build();

        mockMvc.perform(post("/api/user/v1/orderType/add")).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/user/v1/orderType/1")).andExpect(status().isOk());
    }

    /**
     * 班次新增接口已从用户端移除，仅保留查询
     */
    @Test
    void shiftAddEndpoint_shouldNotBeMapped() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserShiftControllerV1(mock(ShiftManager.class)))
                .build();

        mockMvc.perform(post("/api/user/v1/shift/add")).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/user/v1/shift/1")).andExpect(status().isOk());
    }

    /**
     * 订单状态日志新增接口已从用户端移除，仅保留查询
     */
    @Test
    void orderStatusLogAddEndpoint_shouldNotBeMapped() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserOrderStatusLogControllerV1(mock(OrderStatusLogManager.class)))
                .build();

        mockMvc.perform(post("/api/user/v1/orderStatusLog/add")).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/user/v1/orderStatusLog/1")).andExpect(status().isOk());
    }

    /**
     * 用户新增接口已从用户端移除，仅保留查询与本人资料维护
     */
    @Test
    void userAddEndpoint_shouldNotBeMapped() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserUserControllerV1(mock(UserManager.class)))
                .build();

        mockMvc.perform(post("/api/user/v1/user/add")).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/user/v1/user/1")).andExpect(status().isOk());
    }
}
