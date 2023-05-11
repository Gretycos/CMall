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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/4/2 21:49
 */
@RestController
@Tag(name = "Shopping Cart", description = "1-5.商城购物车相关接口")
@RequestMapping("/api")
public class ShoppingCartAPI {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private CouponService couponService;

    @GetMapping("/shopping-cart")
    @Operation(summary = "购物车列表(网页移动端不分页)", description = "")
    public Result<List<ShoppingCartItemVO>> cartItemList(@TokenToMallUser MallUser loginMallUser) {
        return ResultGenerator.genSuccessResult(shoppingCartService.getMyShoppingCartItems(loginMallUser.getUserId()));
    }

    @PostMapping("/shopping-cart")
    @Operation(summary = "添加商品到购物车接口", description = "传参为商品id、数量")
    public Result saveShoppingCartItem(@Parameter(name = "保存购物车项参数") @RequestBody @Valid SaveCartItemParam saveCartItemParam,
                                       @TokenToMallUser MallUser loginMallUser) {
        String saveResult = shoppingCartService.saveShoppingCartItem(saveCartItemParam, loginMallUser.getUserId());
        //添加成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(saveResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //添加失败
        return ResultGenerator.genFailResult(saveResult);
    }

    @PutMapping("/shopping-cart")
    @Operation(summary = "修改购物项数据", description = "传参为购物项id、数量")
    public Result updateShoppingCartItem(@Parameter(name = "更新购物车项参数") @RequestBody @Valid UpdateCartItemParam updateCartItemParam,
                                         @TokenToMallUser MallUser loginMallUser) {
        String updateResult = shoppingCartService.updateShoppingCartItem(updateCartItemParam, loginMallUser.getUserId());
        //修改成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(updateResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //修改失败
        return ResultGenerator.genFailResult(updateResult);
    }

    @DeleteMapping("/shopping-cart/{ShoppingCartItemId}")
    @Operation(summary = "删除购物项", description = "传参为购物项id")
    public Result updateShoppingCartItem(@Parameter(name = "购物车项id") @PathVariable("ShoppingCartItemId") Long shoppingCartItemId,
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

    @GetMapping("/shopping-cart/confirm")
    @Operation(summary = "根据购物项id数组查询购物项明细和可用优惠券", description = "确认订单页面使用")
    public Result<ShoppingCartConfirmVO> confirmCartItem(@Parameter(name = "购物车项id列表") @RequestBody Long[] cartItemIds,
                                                         @TokenToMallUser MallUser loginMallUser) {
        if (cartItemIds.length < 1) {
            CMallException.fail("参数异常");
        }
        BigDecimal priceTotal = new BigDecimal(0);
        List<ShoppingCartItemVO> itemsForConfirmPage = shoppingCartService.getCartItemsForConfirmPage(Arrays.asList(cartItemIds), loginMallUser.getUserId());
        if (CollectionUtils.isEmpty(itemsForConfirmPage)) {
            //无数据则抛出异常
            CMallException.fail("参数异常");
        } else {
            //总价
            for (ShoppingCartItemVO shoppingCartItemVO : itemsForConfirmPage) {
                priceTotal = priceTotal
                        .add(shoppingCartItemVO.getSellingPrice().multiply(new BigDecimal(shoppingCartItemVO.getGoodsCount()))
                        .setScale(2, RoundingMode.HALF_UP));
            }
            if (priceTotal.compareTo(new BigDecimal(1)) < 0) {
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
