package com.tsong.cmall.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    private Long couponId;

    private String couponName;

    private String couponDesc;

    private Integer couponTotal;

    private Integer discount;

    private Integer min;

    private Byte couponLimit;

    private Byte couponType;

    private Byte couponStatus;

    private Byte goodsType;

    private String goodsValue;

    private String couponCode;

    private Date couponStartTime;

    private Date couponEndTime;

    private Date createTime;

    private Date updateTime;

    private Byte isDeleted;
}