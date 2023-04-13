package com.tsong.cmall.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/3/31 14:22
 */
@Data
public class GoodsDetailVO implements Serializable {
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

    @Schema(title = "商品图片")
    private String[] goodsCarouselList;

    @Schema(title = "商品原价")
    private Integer originalPrice;

    @Schema(title = "商品详情字段")
    private String goodsDetailContent;
}
