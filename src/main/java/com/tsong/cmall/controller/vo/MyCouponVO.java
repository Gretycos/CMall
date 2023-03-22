package com.tsong.cmall.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 购物车所用的优惠券VO
 */
@Data
public class MyCouponVO implements Serializable {
    private static final long serialVersionUID = -8182785776876066101L;

    private Long couponUserId;

    private Long userId;

    private Long couponId;

    private String name;

    private String couponDesc;

    private Integer discount;

    private Integer min;

    private Byte goodsType;

    private String goodsValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
}
