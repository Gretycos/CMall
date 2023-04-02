package com.tsong.cmall.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/4/2 23:12
 */
@Data
public class ShoppingCartConfirmVO implements Serializable {
    @ApiModelProperty("参与结算的商品")
    List<ShoppingCartItemVO> itemsForConfirmPage;
    @ApiModelProperty("当前商品可用的优惠券")
    List<MyCouponVO> myCouponVOList;
}
