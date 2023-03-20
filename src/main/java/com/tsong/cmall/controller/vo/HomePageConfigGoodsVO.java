package com.tsong.cmall.controller.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HomePageConfigGoodsVO implements Serializable {
    private Long goodsId;

    private String goodsName;

    private String goodsIntro;

    private String goodsCoverImg;

    private Integer sellingPrice;

    private String tag;
}
