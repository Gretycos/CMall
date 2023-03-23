package com.tsong.cmall.controller.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/3/24 00:32
 */
@Data
public class SearchPageGoodsVO implements Serializable {
    private Long goodsId;

    private String goodsName;

    private String goodsIntro;

    private String goodsCoverImg;

    private Integer sellingPrice;
}
