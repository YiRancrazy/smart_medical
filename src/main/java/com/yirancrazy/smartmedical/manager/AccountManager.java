package com.yirancrazy.smartmedical.manager;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import org.springframework.transaction.annotation.Transactional;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.dto.admin.request.AccountUpdateRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AccountDetailResponse;
import com.yirancrazy.smartmedical.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.yirancrazy.smartmedical.constant.DepartmentConstant.DEPARTMENT_LIST;
import static com.yirancrazy.smartmedical.constant.RoleConstant.ROLE_LIST;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 账户管理器
 * @Datetime: 2026-03-06
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
@Slf4j
public class AccountManager {

    private final AccountService accountService;
    private final AdminService adminService;
    private final UserService userService;
    private final RoleService roleService;
    private final DoctorService doctorService;

    /**
     * 根据用户名、角色ID、是否启用分页查询账户详情
     *
     * @param username 用户名（模糊查询，可为空）
     * @param roleId   角色ID（精确匹配，可为空）
     * @param enabled  是否启用（精确匹配，可为空）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页账户详情响应列表
     */
    public Result<PageInfo<AccountDetailResponse>> listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(
            String username, Long roleId, Boolean enabled, Integer pageNum, Integer pageSize) {

        PageInfo<Account> accounts = accountService.listAllAccountsByRoleIdAndEnabledAndPage(username, roleId, enabled, pageNum, pageSize);

        if (accounts.getList() == null || accounts.getList().isEmpty()) {
            return Result.success(new PageInfo<>(new ArrayList<>()));
        }

        // 根据 roleId 批量加载关联实体
        RelatedEntities entities = loadRelatedEntities(accounts.getList(), roleId);

        // 逐条构建响应
        List<AccountDetailResponse> responseList = accounts.getList().stream()
                .map(account -> buildAccountDetailResponse(account, roleId, entities.adminList, entities.doctorList))
                .collect(Collectors.toList());

        return Result.success(new PageInfo<>(responseList));
    }

    // ========== 子方法 + 内部类 ==========

    /**
     * 关联实体集合：管理员列表 + 医生列表
     */
    private static class RelatedEntities {
        private final List<Admin> adminList;
        private final List<Doctor> doctorList;

        RelatedEntities(List<Admin> adminList, List<Doctor> doctorList) {
            this.adminList = adminList;
            this.doctorList = doctorList;
        }
    }

    /**
     * 根据 roleId 批量加载关联的管理员/医生实体
     */
    private RelatedEntities loadRelatedEntities(List<Account> accounts, Long roleId) {
        List<Doctor> doctorList = new ArrayList<>();
        List<Admin> adminList = new ArrayList<>();

        if (roleId == null) {
            // 未指定 roleId 时按账号实际 roleId 分别拉取
            List<Long> adminUserIds = accounts.stream()
                    .filter(a -> a.getRoleId() != null && a.getRoleId() == 1L)
                    .map(Account::getUserId).toList();
            List<Long> doctorUserIds = accounts.stream()
                    .filter(a -> a.getRoleId() != null && a.getRoleId() == 2L)
                    .map(Account::getUserId).toList();
            if (!adminUserIds.isEmpty()) {
                adminList = adminService.listAdminsByIds(adminUserIds);
            }
            if (!doctorUserIds.isEmpty()) {
                doctorList = doctorService.listDoctorsByIds(doctorUserIds);
            }
        } else if (roleId == 1L) {
            adminList = adminService.listAdminsByIds(accounts.stream().map(Account::getUserId).toList());
        } else if (roleId == 2L) {
            doctorList = doctorService.listDoctorsByIds(accounts.stream().map(Account::getUserId).toList());
        }

        return new RelatedEntities(adminList, doctorList);
    }

    /**
     * 为单个账户构建详情响应
     */
    private AccountDetailResponse buildAccountDetailResponse(Account account, Long roleId,
                                                              List<Admin> adminList, List<Doctor> doctorList) {
        AccountDetailResponse response = new AccountDetailResponse();
        response.setId(String.valueOf(account.getId()));
        response.setUserId(account.getUserId());
        response.setPhone(account.getPhone());
        response.setRoleId(account.getRoleId());
        response.setEnabled(account.getEnabled());
        response.setCreateTime(account.getCreateTime());
        response.setUpdateTime(account.getUpdateTime());

        Long accRole = account.getRoleId();
        boolean isAdmin = Objects.equals(roleId, 1L) || (roleId == null && accRole != null && accRole == 1L);
        boolean isDoctor = Objects.equals(roleId, 2L) || (roleId == null && accRole != null && accRole == 2L);

        if (isAdmin) {
            Admin admin = adminList.stream()
                    .filter(item -> item.getId().equals(account.getUserId()))
                    .findFirst().orElse(null);
            response.setUsername(admin == null ? "" : admin.getName());
            if (admin != null) {
                Department department = DEPARTMENT_LIST.stream()
                        .filter(d -> d.getId().equals(admin.getDepartmentId()))
                        .findFirst().orElse(null);
                response.setDepartmentName(department == null ? "" : department.getName());
                response.setDepartmentId(admin.getDepartmentId());
            } else {
                response.setDepartmentName("");
                response.setDepartmentId(null);
            }
        } else if (isDoctor) {
            Doctor doctor = doctorList.stream()
                    .filter(item -> item.getId().equals(account.getUserId()))
                    .findFirst().orElse(null);
            response.setUsername(doctor == null ? "" : doctor.getName());
            if (doctor != null) {
                Department department = DEPARTMENT_LIST.stream()
                        .filter(d -> d.getId().equals(doctor.getDepartmentId()))
                        .findFirst().orElse(null);
                response.setDepartmentName(department == null ? "" : department.getName());
                response.setDepartmentId(doctor.getDepartmentId());
            } else {
                response.setDepartmentName("");
                response.setDepartmentId(null);
            }
        } else {
            response.setUsername("");
            response.setDepartmentName("");
            response.setDepartmentId(null);
        }

        Role role = ROLE_LIST.stream()
                .filter(r -> r.getId().equals(account.getRoleId()))
                .findFirst().orElse(null);
        if (role != null) {
            response.setRole(role.getRemark());
        }
        return response;
    }

    /**
     * 更新账户（角色 / 启用状态 / 手机号）
     * @param accountId 账户 ID
     * @param request 更新请求体
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateAccount(Long accountId, AccountUpdateRequest request) {
        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            return Result.fail("账户不存在");
        }
        if (request.getRoleId() != null) {
            account.setRoleId(request.getRoleId());
        }
        if (request.getEnabled() != null) {
            account.setEnabled(request.getEnabled());
        }
        if (request.getPhone() != null) {
            account.setPhone(request.getPhone());
        }
        accountService.updateAccountById(account);
        log.info("[account-update] accountId={} -> roleId={}, enabled={}, phone={}",
                accountId, account.getRoleId(), account.getEnabled(), account.getPhone());
        return Result.success(null);
    }

    /**
     * 删除账户（软删除）
     * @param accountId 账户 ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteAccount(Long accountId) {
        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            return Result.fail("账户不存在");
        }
        accountService.deleteAccountById(accountId);
        log.info("[account-delete] accountId={} soft-deleted", accountId);
        return Result.success(null);
    }
}