package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.AccountMapper;
import com.yirancrazy.smartmedical.pojo.Account;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 账户 Service 分页查询语义单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 覆盖用户ID过滤的 null / 空集合 / 非空集合语义
 * @Datetime: 2026-10-01 03:10
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUpTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), Account.class);
    }

    /**
     * null 表示不按用户过滤，不短路查询。
     */
    @Test
    void listAllAccounts_nullUserIdFilter_queriesWithoutUserFilter() {
        Account account = new Account();
        account.setId(1L);
        when(accountMapper.selectList(any())).thenReturn(List.of(account));

        PageInfo<Account> result = accountService
                .listAllAccountsByUserIdFilterAndRoleIdAndEnabledAndPage(null, null, null, 1, 10);

        assertEquals(List.of(account), result.getList());
        verify(accountMapper).selectList(any());
    }

    /**
     * 空集合表示无匹配，直接返回空页且不查询 Mapper。
     */
    @Test
    void listAllAccounts_emptyUserIdFilter_returnsEmptyWithoutQuery() {
        PageInfo<Account> result = accountService
                .listAllAccountsByUserIdFilterAndRoleIdAndEnabledAndPage(List.of(), null, null, 1, 10);

        assertTrue(result.getList().isEmpty());
        verifyNoInteractions(accountMapper);
    }

    /**
     * 非空集合按 user_id in 过滤。
     */
    @Test
    void listAllAccounts_nonEmptyUserIdFilter_buildsInCondition() {
        when(accountMapper.selectList(any())).thenReturn(List.of());

        accountService.listAllAccountsByUserIdFilterAndRoleIdAndEnabledAndPage(
                List.of(11L, 22L), null, null, 1, 10);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<Account>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(accountMapper).selectList(captor.capture());
        LambdaQueryWrapper<Account> wrapper = captor.getValue();
        String sqlSegment = wrapper.getSqlSegment();
        assertTrue(sqlSegment.toUpperCase().contains("USER_ID"));
        assertTrue(sqlSegment.toUpperCase().contains("IN"));
        assertTrue(wrapper.getParamNameValuePairs().values().containsAll(List.of(11L, 22L)));
    }
}
