package com.tsong.cmall.dao;

import com.tsong.cmall.entity.UserCouponRecord;
import com.tsong.cmall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    int getUserCouponCount(Long userId, Long couponId);

    List<UserCouponRecord> selectMyCouponRecords(PageQueryUtil pageQueryUtil);

    Integer countMyCoupons(PageQueryUtil pageQueryUtil);

    List<UserCouponRecord> selectMyAvailableCoupons(Long userId);

    UserCouponRecord getUserCouponByOrderId(Long orderId);

    int expireBatch(@Param("userCouponIds") List<Long> userCouponIds);

}