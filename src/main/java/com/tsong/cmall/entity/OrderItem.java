package com.tsong.cmall.entity;

import java.math.BigDecimal;
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
public class OrderItem {
    private Long orderItemId;

    private Long orderId;

    private Long seckillId;

    private Long goodsId;

    private String goodsName;

    private String goodsCoverImg;

    private BigDecimal sellingPrice;

    private Integer goodsCount;

    private Date createTime;
}