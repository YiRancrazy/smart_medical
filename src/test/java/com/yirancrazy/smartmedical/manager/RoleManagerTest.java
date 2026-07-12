package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.Role;
import com.yirancrazy.smartmedical.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RoleManager 单测
 */
@ExtendWith(MockitoExtension.class)
class RoleManagerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleManager roleManager;

    @Test
    void insertRole_setsSnowflakeIdAndReturnsRole() {
        when(roleService.insertRole(any(Role.class))).thenReturn(1);

        Result<Role> result = roleManager.insertRole("admin", "管理员", "系统管理员");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getId());
        assertEquals("管理员", result.getData().getName());
        assertEquals("系统管理员", result.getData().getRemark());
        verify(roleService).insertRole(any(Role.class));
    }

    @Test
    void insertRole_serviceReturnsZero_returnsFail() {
        when(roleService.insertRole(any(Role.class))).thenReturn(0);

        Result<Role> result = roleManager.insertRole("admin", "管理员", "系统管理员");

        assertEquals(500, result.getCode());
    }

    @Test
    void deleteRoleById_success() {
        when(roleService.deleteRoleById(1L)).thenReturn(1);

        Result<Integer> result = roleManager.deleteRoleById(1L);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData());
    }

    @Test
    void deleteRoleById_zero_returnsFail() {
        when(roleService.deleteRoleById(99L)).thenReturn(0);

        Result<Integer> result = roleManager.deleteRoleById(99L);

        assertEquals(500, result.getCode());
    }

    @Test
    void updateRoleById_success() {
        when(roleService.updateRoleById(any(Role.class))).thenReturn(1);

        Result<Role> result = roleManager.updateRoleById(1L, "管理员2", "改");

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getId());
        assertEquals("管理员2", result.getData().getName());
    }

    @Test
    void updateRoleById_zero_returnsFail() {
        when(roleService.updateRoleById(any(Role.class))).thenReturn(0);

        Result<Role> result = roleManager.updateRoleById(99L, "x", "y");

        assertEquals(500, result.getCode());
    }

    @Test
    void getRoleById_found_returnsSuccess() {
        Role role = new Role();
        role.setId(1L);
        role.setName("管理员");
        when(roleService.getRoleById(1L)).thenReturn(role);

        Result<Role> result = roleManager.getRoleById(1L);

        assertEquals(200, result.getCode());
        assertEquals("管理员", result.getData().getName());
    }

    @Test
    void getRoleById_notFound_returnsFail() {
        when(roleService.getRoleById(99L)).thenReturn(null);

        Result<Role> result = roleManager.getRoleById(99L);

        assertEquals(500, result.getCode());
    }

    @Test
    void listAllRoles_returnsList() {
        Role r1 = new Role();
        r1.setId(1L);
        Role r2 = new Role();
        r2.setId(2L);
        when(roleService.listAllRoles()).thenReturn(List.of(r1, r2));

        Result<List<Role>> result = roleManager.listAllRoles();

        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
    }
}
