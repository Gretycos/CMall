package com.tsong.cmall.dao;

import com.tsong.cmall.entity.Order;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface OrderMapper {
    int deleteByPrimaryKey(Long orderId);

    int insert(Order row);

    int insertSelective(Order row);

    Order selectByPrimaryKey(Long orderId);

    int updateByPrimaryKeySelective(Order row);

    int updateByPrimaryKey(Order row);
}