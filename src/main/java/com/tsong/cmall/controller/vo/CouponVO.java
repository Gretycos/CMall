package com.tsong.cmall.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Author Tsong
 * @Date 2023/3/22 00:24
 */
@Data
public class CouponVO implements Serializable {
    @Schema(title = "优惠券id")
    private Long couponId;

    @Schema(title = "优惠券用户id")
    private Long couponUserId;

    @Schema(title = "优惠券名字")
    private String couponName;

    @Schema(title = "优惠券描述")
    private String couponDesc;

    @Schema(title = "优惠券总数")
    private Integer couponTotal;

    @Schema(title = "优惠券领完")
    private boolean soldOut;

    @Schema(title = "优惠券已用")
    private boolean isUsed;

    @Schema(title = "优惠券折扣")
    private Integer discount;

    @Schema(title = "优惠券最小使用金额")
    private Integer min;

    @Schema(title = "优惠券限制领取数量")
    private Byte couponLimit;

    @Schema(title = "优惠券类型")
    private Byte couponType;

    @Schema(title = "优惠券状态")
    private Byte status;

    @Schema(title = "可用商品类型")
    private Byte goodsType;

    @Schema(title = "可用商品")
    private String goodsValue;

    @Schema(title = "优惠券兑换码")
    private String code;

    @Schema(title = "优惠券开始时间")
    private LocalDate couponStartTime;

    @Schema(title = "优惠券结束时间")
    private LocalDate couponEndTime;

    @Schema(title = "优惠券已领取过")
    private boolean hasReceived;
}
