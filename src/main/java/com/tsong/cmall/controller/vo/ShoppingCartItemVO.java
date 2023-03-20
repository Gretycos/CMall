package com.tsong.cmall.controller.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShoppingCartItemVO implements Serializable {
    private Long cartItemId;

    private Long goodsId;

    private Integer goodsCount;

    private String goodsName;

    private String goodsCoverImg;

    private Integer sellingPrice;
}
