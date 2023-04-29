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
    public Result<List<CouponVO>> availableCouponList(@TokenToMallUser MallUser loginMallUser){
        List<CouponVO> couponVOList = couponService.selectAvailableCoupon(loginMallUser.getUserId());
        return ResultGenerator.genSuccessResult(couponVOList);
    }

    @GetMapping("/coupon/my")
    @Operation(summary = "我的优惠券列表", description = "")
    public Result<PageResult<CouponVO>> myCouponList(@Parameter(name = "页码") @RequestParam(required = false) Integer pageNumber,
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

    @PostMapping("/coupon/save")
    @Operation(summary = "领券", description = "传参为优惠券id，优惠券兑换码（可选）")
    public Result<String> saveCoupon(@Parameter(name = "优惠券id") @RequestParam Long couponId,
                                     @Parameter(name = "优惠券兑换码") @RequestParam(required = false) String couponCode,
                                    @TokenToMallUser MallUser loginMallUser) {
        boolean saveResult = couponService.saveCouponUser(couponId, loginMallUser.getUserId(), couponCode);
        if (saveResult){
            return ResultGenerator.genSuccessResult();
        }
        return ResultGenerator.genFailResult("领券失败");
    }
}
