package com.tsong.cmall.controller.mall;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.config.annotation.TokenToMallUser;
import com.tsong.cmall.controller.mall.param.AddCouponParam;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.CouponService;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Tsong
 * @Date 2023/4/6 15:58
 */
@RestController
@Tag(name = "coupon", description = "1-8.优惠券页面接口")
@RequestMapping("/api")
public class CouponAPI {
    @Autowired
    private CouponService couponService;

    @GetMapping("/coupon")
    @Operation(summary = "可领优惠券列表", description = "")
    public Result availableCouponList(@Parameter(name = "页码") @RequestParam(required = false) Integer pageNumber,
                                      @TokenToMallUser MallUser loginMallUser){
        Map<String, Object> params = new HashMap<>(8);
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        params.put("page", pageNumber);
        params.put("limit", Constants.MY_COUPONS_LIMIT);
        // 封装优惠券分页查询参数
        PageQueryUtil pageUtil = new PageQueryUtil(params);

        return ResultGenerator.genSuccessResult(couponService.selectAvailableCoupon(loginMallUser.getUserId(), pageUtil));
    }

    @GetMapping("/coupon/my")
    @Operation(summary = "我的优惠券列表", description = "能够查询到领券后一个月内的记录")
    public Result myCouponList(@Parameter(name = "页码") @RequestParam(required = false) Integer pageNumber,
                                                     @Parameter(name = "使用状态") @RequestParam(required = false) Byte useStatus,
                               @TokenToMallUser MallUser loginMallUser){
        Map<String, Object> params = new HashMap<>(8);
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        params.put("userId", loginMallUser.getUserId());
        params.put("page", pageNumber);
        params.put("limit", Constants.MY_COUPONS_LIMIT);
        params.put("useStatus", useStatus);
        // 封装优惠券分页查询参数
        PageQueryUtil pageUtil = new PageQueryUtil(params);

        return ResultGenerator.genSuccessResult(couponService.selectMyCoupons(pageUtil));
    }

    @GetMapping("/coupon/available")
    @Operation(summary = "我的所有可用优惠券", description = "能够查询到领券后一个月内的记录")
    public Result allMyAvailableCouponList(@TokenToMallUser MallUser loginMallUser){
        return ResultGenerator.genSuccessResult(couponService.selectAllMyAvailableCoupons(loginMallUser.getUserId()));
    }

    @PostMapping("/coupon/save")
    @Operation(summary = "领券", description = "传参为优惠券id，优惠券兑换码（可选）")
    public Result saveCoupon(@Parameter(name = "优惠券id") @RequestBody AddCouponParam addCouponParam,
                                    @TokenToMallUser MallUser loginMallUser) {
        Long couponId = addCouponParam.getCouponId();
        String couponCode = addCouponParam.getCouponCode();
        if (couponId == null && couponCode == null){
            CMallException.fail("参数错误");
        }
        boolean saveResult = couponService.saveCouponUser(couponId, loginMallUser.getUserId(), couponCode);
        if (saveResult){
            return ResultGenerator.genSuccessResult();
        }
        return ResultGenerator.genFailResult("领券失败");
    }
}
