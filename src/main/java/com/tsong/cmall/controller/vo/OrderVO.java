package com.tsong.cmall.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 订单列表的VO
 * @Author Tsong
 * @Date 2023/3/24 22:49
 */
@Data
public class OrderVO implements Serializable {
    @Schema(title = "订单id")
    private Long orderId;

    @Schema(title = "订单号")
    private String orderNo;

    @Schema(title = "订单总价")
    private Integer totalPrice;

    @Schema(title = "支付类型")
    private Byte payType;

    @Schema(title = "订单状态")
    private Byte orderStatus;

    @Schema(title = "订单状态字符串")
    private String orderStatusString;

    @Schema(title = "创建时间")
    private Date createTime;

    @Schema(title = "订单项目列表")
    private List<OrderItemVO> orderItemVOList;
}
