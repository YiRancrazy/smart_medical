package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Price;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号价格服务接口
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

public interface PriceService {

    /**
     * 添加挂号价格
     * @param price 价格对象
     * @return 添加结果
     */
    Integer insertPrice(Price price);

    /**
     * 根据ID查询挂号价格
     * @param id 价格ID
     * @return 价格对象
     */
    Price getPriceById(Long id);

    /**
     * 根据ID更新挂号价格
     * @param price 价格对象
     * @return 更新结果
     */
    Integer updatePriceById(Price price);

    /**
     * 根据ID删除挂号价格
     * @param id 价格ID
     * @return 删除结果
     */
    Integer deletePriceById(Long id);

    /**
     * 查询所有挂号价格列表
     * @return 价格列表
     */
    List<Price> listAllPrices();

    /**
     * 分页查询挂号价格列表
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<Price> listPricesByPage(Integer pageNum, Integer pageSize);
}
