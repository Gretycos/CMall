package com.tsong.cmall.service;

import com.tsong.cmall.controller.vo.CouponVO;
import com.tsong.cmall.controller.vo.MyCouponVO;
import com.tsong.cmall.controller.vo.ShoppingCartItemVO;
import com.tsong.cmall.entity.Coupon;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    PageResult getCouponPage(PageQueryUtil pageUtil);

    boolean saveCoupon(Coupon coupon);

    boolean updateCoupon(Coupon coupon);

    Coupon getCouponById(Long id);

    boolean deleteCouponById(Long id);

    /**
     * @Description 用户可领优惠券
     * @Param [userId]
     * @Return java.util.List<com.tsong.cmall.controller.vo.CouponVO>
     */
    PageResult selectAvailableCoupon(Long userId, PageQueryUtil pageQueryUtil);

    /**
     * @Description 用户领取优惠券
     * @Param [couponId, userId]
     * @Return boolean
     */
    boolean saveCouponUser(Long couponId, Long userId, String couponCode);

    /**
     * @Description 查询领到的优惠券
     * @Param [pageQueryUtil]
     * @Return com.tsong.cmall.util.PageResult<com.tsong.cmall.controller.vo.CouponVO>
     */
    PageResult<MyCouponVO> selectMyCoupons(PageQueryUtil pageQueryUtil);

    List<MyCouponVO> selectAllMyAvailableCoupons(Long userId);

    /**
     * @Description 查询当前订单可用的优惠券
     * @Param [myShoppingCartItems, priceTotal, userId]
     * @Return java.util.List<com.tsong.cmall.controller.vo.CouponVO>
     */
    List<MyCouponVO> selectCouponsForOrder(List<ShoppingCartItemVO> myShoppingCartItems, BigDecimal priceTotal, Long userId);

    /**
     * @Description 删除优惠券
     * @Param [couponUserId]
     * @Return boolean
     */
    boolean deleteCouponUser(Long couponUserId);

    /**
     * @Description 释放未支付的优惠券
     * @Param [orderId]
     * @Return void
     */
    void releaseCoupon(Long orderId);
}
