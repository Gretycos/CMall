package com.tsong.cmall.dao;

import com.tsong.cmall.entity.Coupon;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface CouponMapper {
    int deleteByPrimaryKey(Long couponId);

    int insert(Coupon row);

    int insertSelective(Coupon row);

    Coupon selectByPrimaryKey(Long couponId);

    int updateByPrimaryKeySelective(Coupon row);

    int updateByPrimaryKey(Coupon row);
}