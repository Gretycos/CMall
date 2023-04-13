package com.tsong.cmall.controller.mall.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/3/30 21:43
 */
@Data
public class UpdateCartItemParam implements Serializable {

    @Schema(title = "购物项id")
    private Long cartItemId;

    @Schema(title = "商品数量")
    private Integer goodsCount;
}
