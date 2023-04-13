package com.tsong.cmall.controller.mall.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/3/31 23:42
 */
@Data
public class SaveOrderParam implements Serializable {
    @Schema(title = "订单项id数组")
    private Long[] cartItemIds;

    @Schema(title = "领券记录id")
    private Long couponUserId;

    @Schema(title = "地址id")
    private Long addressId;
}
