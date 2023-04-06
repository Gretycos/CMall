package com.tsong.cmall.controller.mall;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.config.annotation.TokenToMallUser;
import com.tsong.cmall.controller.vo.CouponVO;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.service.CouponService;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api(value = "coupon", tags = "1-8.优惠券页面接口")
@RequestMapping("/api")
public class CouponAPI {
    @Autowired
    private CouponService couponService;

    @GetMapping("/couponList")
    @ApiOperation(value = "可领优惠券列表", notes = "")
    public Result<List<CouponVO>> availableCouponList(@TokenToMallUser MallUser loginMallUser){
        List<CouponVO> couponVOList = couponService.selectAvailableCoupon(loginMallUser.getUserId());
        return ResultGenerator.genSuccessResult(couponVOList);
    }

    @GetMapping("/myCoupons")
    @ApiOperation(value = "我的优惠券列表", notes = "")
    public Result<PageResult<CouponVO>> myCouponList(@ApiParam(value = "页码") @RequestParam(required = false) Integer pageNumber,
                               @TokenToMallUser MallUser loginMallUser){
        Map<String, Object> params = new HashMap<>(8);
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        params.put("userId", loginMallUser.getUserId());
        params.put("page", pageNumber);
        params.put("limit", Constants.MY_COUPONS_LIMIT);

        // 封装优惠券分页查询参数
        PageQueryUtil pageUtil = new PageQueryUtil(params);

        return ResultGenerator.genSuccessResult(couponService.selectMyCoupons(pageUtil));
    }

    @PostMapping("/saveCoupon")
    @ApiOperation(value = "领券", notes = "传参为地址id、待结算的购物项id数组、领券id")
    public Result<String> saveCoupon(@ApiParam(value = "优惠券id") @RequestParam Long couponId,
                                    @TokenToMallUser MallUser loginMallUser) {
        boolean saveResult = couponService.saveCouponUser(couponId, loginMallUser.getUserId());
        if (saveResult){
            return ResultGenerator.genSuccessResult();
        }
        return ResultGenerator.genFailResult("领券失败");
    }
}
