package com.tsong.cmall.controller.vo;

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
    private Long seckillId;

    private Long goodsId;

    private String goodsName;

    private String goodsIntro;

    private String goodsDetailContent;

    private String goodsCoverImg;

    private Integer sellingPrice;

    private Integer seckillPrice;

    private Date seckillBegin;

    private Date seckillEnd;

    private String seckillBeginTime;

    private String seckillEndTime;

    private Long startDate;

    private Long endDate;
}
