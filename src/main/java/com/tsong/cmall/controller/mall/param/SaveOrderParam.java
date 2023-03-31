package com.tsong.cmall.controller.mall.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/3/31 23:42
 */
@Data
public class SaveOrderParam implements Serializable {
    @ApiModelProperty("订单项id数组")
    private Long[] cartItemIds;

    @ApiModelProperty("领券记录id")
    private Long couponUserId;

    @ApiModelProperty("地址id")
    private Long addressId;
}
