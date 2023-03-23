package com.tsong.cmall.service.impl;

import com.tsong.cmall.controller.vo.CouponVO;
import com.tsong.cmall.controller.vo.MyCouponVO;
import com.tsong.cmall.controller.vo.ShoppingCartItemVO;
import com.tsong.cmall.dao.CouponMapper;
import com.tsong.cmall.dao.GoodsInfoMapper;
import com.tsong.cmall.dao.UserCouponRecordMapper;
import com.tsong.cmall.entity.Coupon;
import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.entity.UserCouponRecord;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.CouponService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toMap;


/**
 * @Author Tsong
 * @Date 2023/3/21 22:58
 */
public class CouponServiceImpl implements CouponService {
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponRecordMapper userCouponRecordMapper;
    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Override
    public PageResult getCouponPage(PageQueryUtil pageUtil) {
        List<Coupon> carousels = couponMapper.findCouponList(pageUtil);
        int total = couponMapper.getTotalCoupons(pageUtil);
        return new PageResult(carousels, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public boolean saveCoupon(Coupon coupon) {
        return couponMapper.insertSelective(coupon) > 0;
    }

    @Override
    public boolean updateCoupon(Coupon coupon) {
        return couponMapper.updateByPrimaryKeySelective(coupon) > 0;
    }

    @Override
    public Coupon getCouponById(Long id) {
        return couponMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean deleteCouponById(Long id) {
        return couponMapper.deleteByPrimaryKey(id) > 0;
    }

    @Override
    public List<CouponVO> selectAvailableCoupon(Long userId) {
        List<Coupon> coupons = couponMapper.selectAvailableCoupon();
        List<CouponVO> couponVOList = BeanUtil.copyList(coupons, CouponVO.class);
        for (CouponVO couponVO : couponVOList) {
            if (userId != null) {
                int num = userCouponRecordMapper.getUserCouponCount(userId, couponVO.getCouponId());
                if (num > 0) {
                    couponVO.setHasReceived(true);
                }
            }
            if (couponVO.getCouponTotal() != 0) { // 0 是无限的意思
                // 没有库存了
                if (couponVO.getCouponTotal() == 1) {
                    couponVO.setSoldOut(true);
                }
            }
        }
        return couponVOList;
    }

    /**
     * @Description 用户领优惠券
     * @Param [couponId, userId]
     * @Return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveCouponUser(Long couponId, Long userId) {
        Coupon coupon = couponMapper.selectByPrimaryKey(couponId);
        if (coupon.getCouponLimit() != 0) { // 0 是该用户可以不限次数领该券
            // 查询该用户获得该券的数量
            int num = userCouponRecordMapper.getUserCouponCount(userId, couponId);
            if (num != 0) {
                throw new CMallException("优惠券已经领过了,无法再次领取！");
            }
        }
        if (coupon.getCouponTotal() == 1) {
            throw new CMallException("优惠券已经领完了！");
        }
        if (coupon.getCouponTotal() != 0) { // 0 是无限张数
            // couponTotal -= 1;
            // 这里where total > 1
            if (couponMapper.reduceCouponTotal(couponId) <= 0) {
                throw new CMallException("优惠券领取失败！");
            }
        }
        // coupon.total > 1 || coupon.total == 0
        UserCouponRecord couponUserRecord = new UserCouponRecord();
        couponUserRecord.setUserId(userId);
        couponUserRecord.setCouponId(couponId);
        return userCouponRecordMapper.insertSelective(couponUserRecord) > 0;
    }

    @Override
    public PageResult<CouponVO> selectMyCoupons(PageQueryUtil pageUtil) {
        Integer total = userCouponRecordMapper.countMyCoupons(pageUtil);
        List<CouponVO> couponVOList = new ArrayList<>();
        if (total > 0) {
            // 从拿券记录中查询我领过的券
            List<UserCouponRecord> userCouponRecordList = userCouponRecordMapper.selectMyCoupons(pageUtil);
            // 获取我领过的券的id集合
            List<Long> couponIdList = userCouponRecordList.stream().map(UserCouponRecord::getCouponId).toList();
            if (!CollectionUtils.isEmpty(couponIdList)) {
                // 用id集合查询券
                List<Coupon> couponList = couponMapper.selectByIds(couponIdList);
                // map，用id找coupon
                // 因为couponVO需要coupon的属性，以及userCoupon的属性
                Map<Long, Coupon> couponMap = couponList.stream().collect(toMap(Coupon::getCouponId, coupon -> coupon));
                for (UserCouponRecord userCouponRecord : userCouponRecordList) {
                    CouponVO couponVO = new CouponVO();
                    Coupon coupon = couponMap.get(userCouponRecord.getCouponId());
                    BeanUtil.copyProperties(coupon, couponVO);
                    couponVO.setCouponUserId(userCouponRecord.getCouponUserId());
                    couponVO.setUsed(userCouponRecord.getUsedTime() != null);
                    couponVOList.add(couponVO);
                }
            }
        }
        return new PageResult<>(couponVOList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public List<MyCouponVO> selectCouponsForOrder(List<ShoppingCartItemVO> myShoppingCartItems, int priceTotal, Long userId) {
        List<UserCouponRecord> userCouponRecordList = userCouponRecordMapper.selectMyAvailableCoupons(userId);
        List<MyCouponVO> myCouponVOList = BeanUtil.copyList(userCouponRecordList, MyCouponVO.class);
        List<Long> couponIds = userCouponRecordList.stream().map(UserCouponRecord::getCouponId).toList();
        if (!couponIds.isEmpty()) {
            // 时区
            ZoneId zone = ZoneId.systemDefault();
            List<Coupon> couponList = couponMapper.selectByIds(couponIds);
            Map<Long, Coupon> couponMap = couponList.stream().collect(toMap(Coupon::getCouponId, coupon -> coupon));
            // 把coupon的值传递给myCouponVO
            for (MyCouponVO myCouponVO : myCouponVOList) {
                Coupon coupon = couponMap.get(myCouponVO.getCouponId());
                if (coupon != null){
                    myCouponVO.setName(coupon.getCouponName());
                    myCouponVO.setCouponDesc(coupon.getCouponDesc());
                    myCouponVO.setDiscount(coupon.getDiscount());
                    myCouponVO.setMin(coupon.getMin());
                    myCouponVO.setGoodsType(coupon.getGoodsType());
                    myCouponVO.setGoodsValue(coupon.getGoodsValue());
                    ZonedDateTime startZonedDateTime =
                            coupon.getCouponStartTime().toInstant().atZone(zone).toLocalDate().atStartOfDay(zone);
                    ZonedDateTime endZonedDateTime =
                            coupon.getCouponEndTime().toInstant().atZone(zone).toLocalDate().atStartOfDay(zone);
                    myCouponVO.setStartTime(Date.from(startZonedDateTime.toInstant()));
                    myCouponVO.setEndTime(Date.from(endZonedDateTime.toInstant()));
                }
            }
        }

        long nowTime = System.currentTimeMillis();
        // 筛选可用的券
        return myCouponVOList.stream().filter(item -> {
            // 判断有效期
            Date startTime = item.getStartTime();
            Date endTime = item.getEndTime();
            if (startTime == null || endTime == null || nowTime < startTime.getTime() || nowTime > endTime.getTime()) {
                return false;
            }
            // 判断使用条件
            boolean isValid = false;
            if (item.getMin() <= priceTotal) {
                if (item.getGoodsType() == 0){ // 全场通用
                    isValid = true;
                } else {
                    String[] split = item.getGoodsValue().split(",");
                    // 券的可用分类id / 商品id
                    Set<Long> goodsValueSet = Arrays.stream(split).map(Long::valueOf).collect(toSet());
                    // 从购物车里查找物品
                    List<Long> goodsIds = myShoppingCartItems.stream().map(ShoppingCartItemVO::getGoodsId).toList();

                    if (item.getGoodsType() == 1) { // 指定分类可用
                        List<GoodsInfo> goodsList = goodsInfoMapper.selectByPrimaryKeys(goodsIds);
                        List<Long> categoryIds = goodsList.stream().map(GoodsInfo::getGoodsCategoryId).toList();
                        for (Long categoryId : categoryIds) {
                            if (goodsValueSet.contains(categoryId)) {
                                isValid = true;
                                break;
                            }
                        }
                    } else if (item.getGoodsType() == 2) { // 指定商品可用
                        for (Long goodsId : goodsIds) {
                            if (goodsValueSet.contains(goodsId)) {
                                isValid = true;
                                break;
                            }
                        }
                    }
                }
            }
            return isValid;
        }).sorted(Comparator.comparingInt(MyCouponVO::getDiscount)).toList();
    }

    @Override
    public boolean deleteCouponUser(Long couponUserId) {
        return userCouponRecordMapper.deleteByPrimaryKey(couponUserId) > 0;
    }

    @Override
    public void releaseCoupon(Long orderId) {
        UserCouponRecord userCouponRecord = userCouponRecordMapper.getUserCouponByOrderId(orderId);
        userCouponRecord.setUseStatus((byte) 0);
        userCouponRecord.setUpdateTime(new Date());
        userCouponRecordMapper.updateByPrimaryKey(userCouponRecord);
    }
}
