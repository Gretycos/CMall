package com.tsong.cmall.controller.mall;

import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.TokenToMallUser;
import com.tsong.cmall.controller.mall.param.SaveCartItemParam;
import com.tsong.cmall.controller.mall.param.UpdateCartItemParam;
import com.tsong.cmall.controller.vo.MyCouponVO;
import com.tsong.cmall.controller.vo.ShoppingCartConfirmVO;
import com.tsong.cmall.controller.vo.ShoppingCartItemVO;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.entity.ShoppingCartItem;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.CouponService;
import com.tsong.cmall.service.ShoppingCartService;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/4/2 21:49
 */
@RestController
@Api(value = "Shopping Cart", tags = "5.商城购物车相关接口")
@RequestMapping("/api")
public class ShoppingCartAPI {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private CouponService couponService;

    @GetMapping("/shop-cart")
    @ApiOperation(value = "购物车列表(网页移动端不分页)", notes = "")
    public Result<List<ShoppingCartItemVO>> cartItemList(@TokenToMallUser MallUser loginMallUser) {
        return ResultGenerator.genSuccessResult(shoppingCartService.getMyShoppingCartItems(loginMallUser.getUserId()));
    }

    @PostMapping("/shop-cart")
    @ApiOperation(value = "添加商品到购物车接口", notes = "传参为商品id、数量")
    public Result saveShoppingCartItem(@RequestBody SaveCartItemParam saveCartItemParam,
                                                 @TokenToMallUser MallUser loginMallUser) {
        String saveResult = shoppingCartService.saveShoppingCartItem(saveCartItemParam, loginMallUser.getUserId());
        //添加成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(saveResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //添加失败
        return ResultGenerator.genFailResult(saveResult);
    }

    @PutMapping("/shop-cart")
    @ApiOperation(value = "修改购物项数据", notes = "传参为购物项id、数量")
    public Result updateShoppingCartItem(@RequestBody UpdateCartItemParam updateCartItemParam,
                                                   @TokenToMallUser MallUser loginMallUser) {
        String updateResult = shoppingCartService.updateShoppingCartItem(updateCartItemParam, loginMallUser.getUserId());
        //修改成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(updateResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //修改失败
        return ResultGenerator.genFailResult(updateResult);
    }

    @DeleteMapping("/shop-cart/{newBeeMallShoppingCartItemId}")
    @ApiOperation(value = "删除购物项", notes = "传参为购物项id")
    public Result updateShoppingCartItem(@PathVariable("newBeeMallShoppingCartItemId") Long shoppingCartItemId,
                                                   @TokenToMallUser MallUser loginMallUser) {
        ShoppingCartItem shoppingCartItem = shoppingCartService.getShoppingCartItemById(shoppingCartItemId);
        if (!loginMallUser.getUserId().equals(shoppingCartItem.getUserId())) {
            return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        }
        Boolean deleteResult = shoppingCartService.deleteById(shoppingCartItemId,loginMallUser.getUserId());
        //删除成功
        if (deleteResult) {
            return ResultGenerator.genSuccessResult();
        }
        //删除失败
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }

    @GetMapping("/shop-cart/confirm")
    @ApiOperation(value = "根据购物项id数组查询购物项明细和可用优惠券", notes = "确认订单页面使用")
    public Result<ShoppingCartConfirmVO> confirmCartItem(Long[] cartItemIds, @TokenToMallUser MallUser loginMallUser) {
        if (cartItemIds.length < 1) {
            CMallException.fail("参数异常");
        }
        int priceTotal = 0;
        List<ShoppingCartItemVO> itemsForConfirmPage = shoppingCartService.getCartItemsForConfirmPage(Arrays.asList(cartItemIds), loginMallUser.getUserId());
        if (CollectionUtils.isEmpty(itemsForConfirmPage)) {
            //无数据则抛出异常
            CMallException.fail("参数异常");
        } else {
            //总价
            for (ShoppingCartItemVO shoppingCartItemVO : itemsForConfirmPage) {
                priceTotal += shoppingCartItemVO.getGoodsCount() * shoppingCartItemVO.getSellingPrice();
            }
            if (priceTotal < 1) {
                CMallException.fail("价格异常");
            }
        }
        List<MyCouponVO> myCouponVOList = couponService.selectCouponsForOrder(
                itemsForConfirmPage, priceTotal, loginMallUser.getUserId());
        ShoppingCartConfirmVO shoppingCartConfirmVO = new ShoppingCartConfirmVO();
        shoppingCartConfirmVO.setItemsForConfirmPage(itemsForConfirmPage);
        shoppingCartConfirmVO.setMyCouponVOList(myCouponVOList);
        return ResultGenerator.genSuccessResult(shoppingCartConfirmVO);
    }
}
