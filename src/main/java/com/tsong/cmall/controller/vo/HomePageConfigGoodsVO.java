package com.tsong.cmall.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class HomePageConfigGoodsVO implements Serializable {
    @Schema(title = "商品id")
    private Long goodsId;

    @Schema(title = "商品名称")
    private String goodsName;

    @Schema(title = "商品简介")
    private String goodsIntro;

    @Schema(title = "商品图片地址")
    private String goodsCoverImg;

    @Schema(title = "商品价格")
    private Integer sellingPrice;

    @Schema(title = "商品标签")
    private String tag;
}
