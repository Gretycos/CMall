package com.tsong.cmall.dao;

import com.tsong.cmall.entity.Coupon;
import com.tsong.cmall.util.PageQueryUtil;

import java.util.List;

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

    Coupon selectByCode(String code);

    int deleteBatch(Integer[] couponIds);

    List<Coupon> findCouponList(PageQueryUtil pageUtil);

    int getTotalCoupons(PageQueryUtil pageUtil);

    List<Coupon> selectAvailableCoupon(PageQueryUtil pageUtil);

    int getTotalAvailableCoupons(PageQueryUtil pageUtil);

    int reduceCouponTotal(Long couponId);

    List<Coupon> selectByIds(List<Long> couponIds);

    List<Coupon> selectAvailableGivenCoupon();
}