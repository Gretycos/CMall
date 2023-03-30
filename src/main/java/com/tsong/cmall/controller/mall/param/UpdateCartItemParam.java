package com.tsong.cmall.controller.mall.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/3/30 21:43
 */
@Data
public class UpdateCartItemParam implements Serializable {

    @ApiModelProperty("购物项id")
    private Long cartItemId;

    @ApiModelProperty("商品数量")
    private Integer goodsCount;
}
