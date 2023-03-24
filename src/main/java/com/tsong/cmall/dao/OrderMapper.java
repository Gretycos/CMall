package com.tsong.cmall.dao;

import com.tsong.cmall.entity.Order;
import com.tsong.cmall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    Order selectByOrderNo(String orderNo);

    List<Order> findOrderList(PageQueryUtil pageUtil);

    int getTotalOrders(PageQueryUtil pageUtil);

    List<Order> selectByPrimaryKeys(List<Long> orderIds);

    int checkOut(List<Long> orderIds);

    int closeOrder(List<Long> orderIds, int orderStatus);

    int checkDone(List<Long> asList);

    List<Order> selectPrePaidOrders();
}