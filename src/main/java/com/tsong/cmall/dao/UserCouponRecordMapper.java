package com.tsong.cmall.dao;

import com.tsong.cmall.entity.UserCouponRecord;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface UserCouponRecordMapper {
    int deleteByPrimaryKey(Long couponUserId);

    int insert(UserCouponRecord row);

    int insertSelective(UserCouponRecord row);

    UserCouponRecord selectByPrimaryKey(Long couponUserId);

    int updateByPrimaryKeySelective(UserCouponRecord row);

    int updateByPrimaryKey(UserCouponRecord row);
}