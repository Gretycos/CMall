package com.tsong.cmall.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/3/24 00:32
 */
@Data
public class SearchPageGoodsVO implements Serializable {
    @ApiModelProperty("商品id")
    private Long goodsId;

    @ApiModelProperty("商品名称")
    private String goodsName;

    @ApiModelProperty("商品简介")
    private String goodsIntro;

    @ApiModelProperty("商品图片地址")
    private String goodsCoverImg;

    @ApiModelProperty("商品价格")
    private Integer sellingPrice;
}
