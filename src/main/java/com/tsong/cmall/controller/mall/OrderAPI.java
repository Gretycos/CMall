package com.tsong.cmall.controller.mall;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.NoRepeatSubmit;
import com.tsong.cmall.config.annotation.TokenToMallUser;
import com.tsong.cmall.controller.mall.param.SaveOrderParam;
import com.tsong.cmall.controller.mall.param.SaveSeckillOrderParam;
import com.tsong.cmall.controller.vo.OrderDetailVO;
import com.tsong.cmall.controller.vo.OrderVO;
import com.tsong.cmall.controller.vo.ShoppingCartItemVO;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.entity.UserAddress;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.OrderService;
import com.tsong.cmall.service.ShoppingCartService;
import com.tsong.cmall.service.UserAddressService;
import com.tsong.cmall.util.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Tsong
 * @Date 2023/3/31 23:40
 */
@RestController
@Tag(name = "order", description = "1-7.订单操作相关接口")
@RequestMapping("/api")
public class OrderAPI {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserAddressService userAddressService;

    @NoRepeatSubmit
    @PostMapping("/order/save")
    @Operation(summary = "提交订单接口", description = "传参为地址id、待结算的购物项id数组、领券id")
    public Result<String> saveOrder(@Parameter(name = "订单参数") @RequestBody SaveOrderParam saveOrderParam,
                                    @TokenToMallUser MallUser loginMallUser) {
        if (saveOrderParam == null
                || saveOrderParam.getCartItemIds() == null
                || saveOrderParam.getAddressId() == null
                || saveOrderParam.getCouponUserId() == null) {
            CMallException.fail(ServiceResultEnum.PARAM_ERROR.getResult());
        }
        if (saveOrderParam.getCartItemIds().length < 1) {
            CMallException.fail(ServiceResultEnum.PARAM_ERROR.getResult());
        }

        // 结算页的物品列表
        List<ShoppingCartItemVO> itemsForConfirmPage = shoppingCartService.getCartItemsForConfirmPage(
                Arrays.asList(saveOrderParam.getCartItemIds()), loginMallUser.getUserId());
        if (CollectionUtils.isEmpty(itemsForConfirmPage)) {
            //无数据
            CMallException.fail("参数异常");
        } else {
            UserAddress address = userAddressService.getUserAddressById(saveOrderParam.getAddressId());
            if (!loginMallUser.getUserId().equals(address.getUserId())) {
                return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
            }
            Long couponUserId = saveOrderParam.getCouponUserId();
            //保存订单并返回订单号
            String saveOrderResult = orderService.saveOrder(loginMallUser, couponUserId, address, itemsForConfirmPage);
            Result result = ResultGenerator.genSuccessResult();
            result.setData(saveOrderResult);
            return result;
        }
        return ResultGenerator.genFailResult("生成订单失败");
    }

    @NoRepeatSubmit
    @PostMapping("/order/seckill/save")
    @Operation(summary = "提交订单接口", description = "传参为地址id、待结算的购物项id数组、领券id")
    public Result<String> saveOrder(@Parameter(name = "订单参数") @RequestBody SaveSeckillOrderParam saveSeckillOrderParam,
                                    @TokenToMallUser MallUser loginMallUser) {
        if (saveSeckillOrderParam == null
                || saveSeckillOrderParam.getSeckillSuccessId() == null
                || saveSeckillOrderParam.getAddressId() == null
                || saveSeckillOrderParam.getSeckillSecretKey() == null) {
            CMallException.fail(ServiceResultEnum.PARAM_ERROR.getResult());
        }
        Long seckillSuccessId = saveSeckillOrderParam.getSeckillSuccessId();
        String seckillSecretKey = saveSeckillOrderParam.getSeckillSecretKey();
        if (!seckillSecretKey.equals(MD5Util.MD5Encode(seckillSuccessId + Constants.SECKILL_ORDER_SALT, Constants.UTF_ENCODING))) {
            CMallException.fail("秒杀订单不合法");
        }
        UserAddress address = userAddressService.getUserAddressById(saveSeckillOrderParam.getAddressId());
        String saveOrderResult = orderService.seckillSaveOrder(seckillSuccessId, loginMallUser.getUserId(),address);
        Result result = ResultGenerator.genSuccessResult();
        result.setData(saveOrderResult);
        return result;
    }

    @GetMapping("/order/{orderNo}")
    @Operation(summary = "订单详情接口", description = "传参为订单号")
    public Result<OrderDetailVO> orderDetailPage(@Parameter(name = "订单号") @PathVariable("orderNo") String orderNo,
                                                 @TokenToMallUser MallUser loginMallUser) {
        return ResultGenerator.genSuccessResult(orderService.getOrderDetailByOrderNo(orderNo, loginMallUser.getUserId()));
    }

    @GetMapping("/order")
    @Operation(summary = "订单列表接口", description = "传参为页码")
    public Result<PageResult<OrderVO>> orderList(@Parameter(name = "页码") @RequestParam(required = false) Integer pageNumber,
                                                 @Parameter(name = "订单状态:0.待支付 1.待确认 2.待发货 3:已发货 4.交易成功") @RequestParam(required = false) Integer status,
                                                 @TokenToMallUser MallUser loginMallUser) {
        Map<String, Object> params = new HashMap<>(8);
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        params.put("userId", loginMallUser.getUserId());
        params.put("orderStatus", status);
        params.put("page", pageNumber);
        params.put("limit", Constants.MY_ORDERS_PAGE_LIMIT);
        //封装分页请求参数
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(orderService.getMyOrders(pageUtil));
    }

    @PutMapping("/order/cancel/{orderNo}")
    @Operation(summary = "订单取消接口", description = "传参为订单号")
    public Result cancelOrder(@Parameter(name = "订单号") @PathVariable("orderNo") String orderNo,
                              @TokenToMallUser MallUser loginMallUser) {
        String cancelOrderResult = orderService.cancelOrder(orderNo, loginMallUser.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(cancelOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(cancelOrderResult);
        }
    }

    @PutMapping("/order/finish/{orderNo}")
    @Operation(summary = "确认收货接口", description = "传参为订单号")
    public Result finishOrder(@Parameter(name = "订单号") @PathVariable("orderNo") String orderNo,
                              @TokenToMallUser MallUser loginMallUser) {
        String finishOrderResult = orderService.finishOrder(orderNo, loginMallUser.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(finishOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(finishOrderResult);
        }
    }

    @NoRepeatSubmit
    @GetMapping("/paySuccess")
    @Operation(summary = "模拟支付成功回调的接口", description = "传参为订单号和支付方式")
    public Result paySuccess(@Parameter(name = "订单号") @RequestParam("orderNo") String orderNo,
                             @Parameter(name = "支付方式") @RequestParam("payType") int payType) {
        String payResult = orderService.paySuccess(orderNo, payType);
        if (ServiceResultEnum.SUCCESS.getResult().equals(payResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(payResult);
        }
    }

}
