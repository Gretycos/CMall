package com.tsong.cmall.controller.vo;

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
    private Long orderId;

    private String orderNo;

    private Integer totalPrice;

    private Byte payType;

    private Byte orderStatus;

    private String orderStatusString;

    private String userAddress;

    private Date createTime;

    private List<OrderItemVO> orderItemVOList;
}
