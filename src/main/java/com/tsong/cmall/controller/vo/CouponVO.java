package com.tsong.cmall.controller.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Author Tsong
 * @Date 2023/3/22 00:24
 */
@Data
public class CouponVO implements Serializable {
    private Long couponId;

    private Long couponUserId;

    private String couponName;

    private String couponDesc;

    private Integer couponTotal;

    private boolean saleOut;

    private boolean isUsed;

    private Integer discount;

    private Integer min;

    private Byte couponLimit;

    private Byte couponType;

    private Byte status;

    private Byte goodsType;

    private String goodsValue;

    private String code;

    private LocalDate couponStartTime;

    private LocalDate couponEndTime;

    private boolean hasReceived;
}
