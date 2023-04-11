package com.tsong.cmall.controller.admin;

import com.tsong.cmall.config.annotation.TokenToAdminUser;
import com.tsong.cmall.controller.admin.param.CouponAddParam;
import com.tsong.cmall.controller.admin.param.CouponEditParam;
import com.tsong.cmall.entity.AdminUserToken;
import com.tsong.cmall.entity.Coupon;
import com.tsong.cmall.service.CouponService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Tsong
 * @Date 2023/4/11 13:28
 */
@RestController
@Api(value = "Admin Coupon", tags = "2-8.后台管理优惠券模块接口")
@RequestMapping("/admin")
public class AdminCouponAPI {

    private static final Logger logger = LoggerFactory.getLogger(AdminCouponAPI.class);

    @Autowired
    private CouponService couponService;

    @GetMapping("/coupon/list")
    @ApiOperation(value = "优惠券列表", notes = "")
    public Result couponList(@ApiParam(value = "页码") @RequestParam(required = false) Integer pageNumber,
                             @ApiParam(value = "每页条数") @RequestParam(required = false) Integer pageSize,
                             @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("couponList, adminUser:{}", adminUser.toString());
        Map<String, Object> params = new HashMap<>(8);
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize == null || pageSize < 10 || pageSize > 100){
            pageSize = 10;
        }
        params.put("page", pageNumber);
        params.put("limit", pageSize);
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(couponService.getCouponPage(pageUtil));
    }

    @PostMapping("/coupon/save")
    @ApiOperation(value = "新增优惠券", notes = "")
    public Result saveCoupon(@ApiParam(value = "优惠券新增参数") @RequestBody @Valid CouponAddParam couponAddParam,
                             @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("couponSave, adminUser:{}", adminUser.toString());
        Coupon coupon = new Coupon();
        BeanUtil.copyProperties(couponAddParam, coupon);
        coupon.setCreateTime(new Date());
        boolean result = couponService.saveCoupon(coupon);
        if (!result){
            return ResultGenerator.genFailResult("新增优惠券失败");
        }
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/coupon/update")
    @ApiOperation(value = "修改优惠券", notes = "")
    public Result updateCoupon(@ApiParam(value = "优惠券修改参数") @RequestBody @Valid CouponEditParam couponEditParam,
                               @TokenToAdminUser AdminUserToken adminUser){
        logger.info("couponUpdate, adminUser:{}", adminUser.toString());
        Coupon coupon = new Coupon();
        BeanUtil.copyProperties(couponEditParam, coupon);
        coupon.setUpdateTime(new Date());
        boolean result = couponService.updateCoupon(coupon);
        if (!result){
            return ResultGenerator.genFailResult("更新优惠券失败");
        }
        return ResultGenerator.genSuccessResult();
    }


    @GetMapping("/coupon/{id}")
    @ApiOperation(value = "优惠券详情", notes = "")
    public Result couponInfo(@ApiParam(value = "优惠券id") @PathVariable("id") Long id,
                       @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("couponInfo, adminUser:{}", adminUser.toString());
        Coupon coupon = couponService.getCouponById(id);
        return ResultGenerator.genSuccessResult(coupon);
    }

    @DeleteMapping("/coupon/{id}")
    @ApiOperation(value = "删除优惠券", notes = "")
    public Result couponDelete(@ApiParam(value = "优惠券id") @PathVariable Long id,
                         @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("couponDelete, adminUser:{}", adminUser.toString());
        boolean result = couponService.deleteCouponById(id);
        if (!result){
            return ResultGenerator.genFailResult("删除优惠券失败");
        }
        return ResultGenerator.genSuccessResult();
    }
}
