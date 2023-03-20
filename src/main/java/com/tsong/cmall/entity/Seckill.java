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
public class Seckill {
    private Long seckillId;

    private Long goodsId;

    private Integer seckillPrice;

    private Integer seckillNum;

    private Boolean seckillStatus;

    private Date seckillBegin;

    private Date seckillEnd;

    private Integer seckillRank;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted;
}