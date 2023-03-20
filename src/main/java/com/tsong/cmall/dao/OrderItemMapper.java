package com.tsong.cmall.dao;

import com.tsong.cmall.entity.OrderItem;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface OrderItemMapper {
    int deleteByPrimaryKey(Long orderItemId);

    int insert(OrderItem row);

    int insertSelective(OrderItem row);

    OrderItem selectByPrimaryKey(Long orderItemId);

    int updateByPrimaryKeySelective(OrderItem row);

    int updateByPrimaryKey(OrderItem row);
}