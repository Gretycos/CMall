package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.Constants;
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
import com.tsong.cmall.util.MD5Util;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toMap;


/**
 * @Author Tsong
 * @Date 2023/3/21 22:58
 */
@Service
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
        if(coupon.getCouponType() == 2){
            String code = MD5Util.MD5Encode(Constants.COUPON_CODE + System.currentTimeMillis(), Constants.UTF_ENCODING)
                    .substring(8, 24);
            coupon.setCouponCode(code);
        }
        Date startDate = coupon.getCouponStartTime();
        Date endDate = coupon.getCouponEndTime();
        if (startDate != null && endDate != null && endDate.getTime() < startDate.getTime()){
            CMallException.fail("结束时间小于开始时间");
        }
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
    public PageResult selectAvailableCoupon(Long userId, PageQueryUtil pageUtil) {
        List<Coupon> coupons = couponMapper.selectAvailableCoupon(pageUtil);
        List<CouponVO> couponVOList = BeanUtil.copyList(coupons, CouponVO.class);
        int total = couponMapper.getTotalAvailableCoupons(pageUtil);
        if (total > 0){
            for (CouponVO couponVO : couponVOList) {
                if (userId != null) {
                    // 查找领券记录
                    int num = userCouponRecordMapper.getUserCouponCount(userId, couponVO.getCouponId());
                    if (num > 0) {
                        // 领过券了
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
        }
        return new PageResult(couponVOList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    /**
     * @Description 用户领优惠券
     * @Param [couponId, userId]
     * @Return boolean
     */
    @Override
    @Transactional
    public boolean saveCouponUser(Long couponId, Long userId, String couponCode) {
        Coupon coupon;
        if (couponCode != null){
            coupon = couponMapper.selectByCode(couponCode);
            if (coupon == null){
                CMallException.fail("优惠券不存在！");
            }
        } else {
            coupon = couponMapper.selectByPrimaryKey(couponId);
        }
        if (coupon.getCouponLimit() != 0) { // 0 是该用户可以不限次数领该券
            // 查询该用户获得该券的数量
            int num = userCouponRecordMapper.getUserCouponCount(userId, coupon.getCouponId());
            if (num != 0) {
                CMallException.fail("优惠券已经领过了,无法再次领取！");
            }
        }
        if (coupon.getCouponTotal() == 1) {
            CMallException.fail("优惠券已经领完了！");
        }
        if (coupon.getCouponTotal() != 0) { // 0 是无限张数
            // couponTotal -= 1;
            // 这里where total > 1
            if (couponMapper.reduceCouponTotal(coupon.getCouponId()) <= 0) {
                CMallException.fail("优惠券领取失败！");
            }
        }
        // coupon.total > 1 || coupon.total == 0
        UserCouponRecord couponUserRecord = new UserCouponRecord();
        couponUserRecord.setUserId(userId);
        couponUserRecord.setCouponId(coupon.getCouponId());
        return userCouponRecordMapper.insertSelective(couponUserRecord) > 0;
    }

    @Override
    public PageResult selectMyCoupons(PageQueryUtil pageUtil) {
        int total = userCouponRecordMapper.countMyCoupons(pageUtil);
        List<MyCouponVO> myCouponVOList = new ArrayList<>();
        if (total > 0) {
            // 从拿券记录中查询我领过的券
            List<UserCouponRecord> userCouponRecordList = userCouponRecordMapper.selectMyCouponRecords(pageUtil);
            // 从领券记录转化成用户领券视图
            getMyCouponVOList(myCouponVOList, userCouponRecordList);
        }
        return new PageResult(myCouponVOList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public List<MyCouponVO> selectAllMyAvailableCoupons(Long userId) {
        List<UserCouponRecord> userCouponRecordList = userCouponRecordMapper.selectMyAvailableCoupons(userId);
        List<Long> couponIds = userCouponRecordList.stream().map(UserCouponRecord::getCouponId).toList();
        List<MyCouponVO> myCouponVOList = new ArrayList<>();
        if (!couponIds.isEmpty()) {
            // 从领券记录转化成用户领券视图
            getMyCouponVOList(myCouponVOList, userCouponRecordList);
        }
        return myCouponVOList;
    }

    @Override
    public List<MyCouponVO> selectCouponsForOrder(List<ShoppingCartItemVO> myShoppingCartItems, BigDecimal priceTotal, Long userId) {
        List<UserCouponRecord> userCouponRecordList = userCouponRecordMapper.selectMyAvailableCoupons(userId);
        List<Long> couponIds = userCouponRecordList.stream().map(UserCouponRecord::getCouponId).toList();
        List<MyCouponVO> myCouponVOList = new ArrayList<>();
        if (!couponIds.isEmpty()) {
            // 从领券记录转化成用户领券视图
            getMyCouponVOList(myCouponVOList, userCouponRecordList);
        }

        long nowTime = new Date().getTime();
        // 筛选可用的券
        return myCouponVOList.stream().filter(item -> {
            // 排除过期的和未开始的券
            if (item.getUseStatus() != 0 || (item.getCouponStartTime() !=null && nowTime < item.getCouponStartTime().getTime())) {
                return false;
            }
            // 判断使用条件
            boolean isValid = false;
            if (priceTotal.compareTo(new BigDecimal(item.getMin())) >= 0) {
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
                        // 分类id集
                        Set<Long> categoryIds = goodsList.stream().map(GoodsInfo::getGoodsCategoryId).collect(toSet());
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

    private void getMyCouponVOList(List<MyCouponVO> myCouponVOList, List<UserCouponRecord> userCouponRecordList){
        // 获取用户领券的id集合
        List<Long> couponIdList = userCouponRecordList.stream().map(UserCouponRecord::getCouponId).toList();
        if (!CollectionUtils.isEmpty(couponIdList)) {
            Date now = new Date();
            Calendar sevenDaysAgo = Calendar.getInstance();
            sevenDaysAgo.setTime(now);
            sevenDaysAgo.add(Calendar.DATE, -7);
            // 用id集合查询券
            List<Coupon> couponList = couponMapper.selectByIds(couponIdList);
            // 未使用但过期的已领券
            List<UserCouponRecord> expiredAndNotUsedUserCouponRecordList = new ArrayList<>();
            // map，用id找coupon
            // 因为couponVO需要coupon的属性，以及userCoupon的属性
            Map<Long, Coupon> couponMap = couponList.stream().collect(toMap(Coupon::getCouponId, coupon -> coupon));
            for (UserCouponRecord userCouponRecord : userCouponRecordList) {
                MyCouponVO myCouponVO = new MyCouponVO();
                Coupon coupon = couponMap.get(userCouponRecord.getCouponId());
                if (coupon != null){
                    BeanUtil.copyProperties(coupon, myCouponVO);
                    myCouponVO.setCouponUserId(userCouponRecord.getCouponUserId());
                    myCouponVO.setCouponUserCreateTime(userCouponRecord.getCreateTime());
                    // 优惠券过期未使用
                    if (userCouponRecord.getUseStatus() != 1
                            // 如果没有过期时间则领取7日之后过期
                            // 如果有过期时间，则过期时间之后过期
                            && ((coupon.getCouponEndTime() == null && userCouponRecord.getCreateTime().getTime() < sevenDaysAgo.getTimeInMillis())
                                || (coupon.getCouponEndTime() != null && now.getTime() > coupon.getCouponEndTime().getTime()))
                    ){
                        myCouponVO.setUseStatus((byte) 2);
                        expiredAndNotUsedUserCouponRecordList.add(userCouponRecord);
                    } else {
                        myCouponVO.setUseStatus(userCouponRecord.getUseStatus());
                    }
                    myCouponVOList.add(myCouponVO);
                }
            }
            if (!expiredAndNotUsedUserCouponRecordList.isEmpty()){
                List<Long> userCouponRecordIds = expiredAndNotUsedUserCouponRecordList.stream()
                        .map(UserCouponRecord::getCouponUserId).toList();
                if (userCouponRecordMapper.expireBatch(userCouponRecordIds) <= 0){
                    CMallException.fail("设置用户已领券失效失败");
                }
            }
        }
    }

    @Override
    public boolean deleteCouponUser(Long couponUserId) {
        return userCouponRecordMapper.deleteByPrimaryKey(couponUserId) > 0;
    }

    @Override
    public void releaseCoupon(Long orderId) {
        UserCouponRecord userCouponRecord = userCouponRecordMapper.getUserCouponByOrderId(orderId);
        if (userCouponRecord != null){
            userCouponRecord.setUseStatus((byte) 0);
            userCouponRecord.setUpdateTime(new Date());
            userCouponRecordMapper.updateByPrimaryKey(userCouponRecord);
        }
    }
}
