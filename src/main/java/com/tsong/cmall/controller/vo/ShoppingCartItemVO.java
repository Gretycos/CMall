package com.tsong.cmall.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ShoppingCartItemVO implements Serializable {
    @Schema(title = "购物车id")
    private Long cartItemId;

    @Schema(title = "商品id")
    private Long goodsId;

    @Schema(title = "商品数量")
    private Integer goodsCount;

    @Schema(title = "商品名")
    private String goodsName;

    @Schema(title = "商品封面图")
    private String goodsCoverImg;

    @Schema(title = "商品价格")
    private Integer sellingPrice;
}
