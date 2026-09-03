package com.yirancrazy.smartmedical.manager;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderType;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.OutpatientFeeItemResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.OrderTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 门诊费用业务编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端门诊费用列表：聚合订单 + 类型/状态名称翻译
 * @Datetime: 2026-07-25 14:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class OutpatientFeeManager {

    /** 订单类型:挂号订单(DDL order_type.id=1)、药品订单(DDL order_type.id=2) */
    private static final List<Long> OUTPATIENT_FEE_ORDER_TYPES = List.of(1L, 2L);

    private final OrderService orderService;
    private final OrderTypeService orderTypeService;

    /**
     * 查询当前用户的门诊费用订单列表
     * @param userId 当前用户ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页订单列表
     */
    public Result<PageResult<OutpatientFeeItemResponse>> listMyOrders(Long userId, Integer current, Integer size) {
        int pageNum = current == null || current < 1 ? 1 : current;
        int pageSize = size == null || size < 1 ? 10 : size;

        Map<Long, String> orderTypeNameMap = loadOrderTypeNameMap();

        PageHelper.startPage(pageNum, pageSize);
        List<Order> orders = orderService.getOrdersByUserIdAndOrderTypeIds(userId, OUTPATIENT_FEE_ORDER_TYPES);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);

        if (orders.isEmpty()) {
            return Result.success(new PageResult<>(pageInfo, Collections.emptyList()));
        }

        List<OutpatientFeeItemResponse> list = orders.stream()
                .map(order -> convertToResponse(order, orderTypeNameMap))
                .collect(Collectors.toList());
        return Result.success(new PageResult<>(pageInfo, list));
    }

    /**
     * 加载订单类型名称映射
     * @return id -> name
     */
    private Map<Long, String> loadOrderTypeNameMap() {
        return orderTypeService.listAllOrderTypes().stream()
                .collect(Collectors.toMap(OrderType::getId, OrderType::getName, (n1, n2) -> n1));
    }

    /**
     * 订单实体转列表响应
     * @param order 订单实体
     * @param orderTypeNameMap 类型名称映射
     * @return 列表项
     */
    private OutpatientFeeItemResponse convertToResponse(Order order, Map<Long, String> orderTypeNameMap) {
        OutpatientFeeItemResponse item = new OutpatientFeeItemResponse();
        item.setOrderId(order.getId() == null ? null : String.valueOf(order.getId()));
        item.setOrderSn(order.getSn() == null ? null : String.valueOf(order.getSn()));
        item.setOrderTypeId(order.getOrderTypeId() == null ? null : String.valueOf(order.getOrderTypeId()));
        item.setOrderTypeName(orderTypeNameMap.getOrDefault(order.getOrderTypeId(), "未知类型"));
        item.setStatus(order.getStatus());
        item.setStatusName(resolveStatusName(order.getStatus()));
        item.setTotalAmount(order.getTotalAmount());
        item.setCreateTime(order.getCreateTime());
        return item;
    }

    /**
     * 根据状态码解析状态名称
     * @param status 状态码
     * @return 状态名称
     */
    private String resolveStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        for (OrderStatus os : OrderStatus.values()) {
            if (os.getCode() == status) {
                return os.getDescription();
            }
        }
        return "未知";
    }
}
