package com.tsong.cmall.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author Tsong
 * @Date 2023/3/25 14:08
 */
@Data
public class SeckillGoodsVO implements Serializable {
    private static final long serialVersionUID = -8719192110998138980L;

    @Schema(title = "秒杀id")
    private Long seckillId;

    @Schema(title = "商品id")
    private Long goodsId;

    @Schema(title = "商品名")
    private String goodsName;

    @Schema(title = "商品简介")
    private String goodsIntro;

    @Schema(title = "商品细节")
    private String goodsDetailContent;

    @Schema(title = "商品封面图")
    private String goodsCoverImg;

    @Schema(title = "商品售价")
    private Integer sellingPrice;

    @Schema(title = "商品秒杀价")
    private Integer seckillPrice;

    @Schema(title = "秒杀开始")
    private Date seckillBegin;

    @Schema(title = "秒杀结束")
    private Date seckillEnd;

    @Schema(title = "秒杀开始时间戳")
    private Long startDate;

    @Schema(title = "秒杀结束时间戳")
    private Long endDate;
}
