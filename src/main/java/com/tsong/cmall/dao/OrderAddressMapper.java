package com.tsong.cmall.dao;

import com.tsong.cmall.entity.OrderAddress;

/**
 * @Author: Tsong
 * @date: 2023/03/27/05:14
 */
public interface OrderAddressMapper {
    int deleteByPrimaryKey(Long orderId);

    int insert(OrderAddress row);

    int insertSelective(OrderAddress row);

    OrderAddress selectByPrimaryKey(Long orderId);

    int updateByPrimaryKeySelective(OrderAddress row);

    int updateByPrimaryKey(OrderAddress row);
}