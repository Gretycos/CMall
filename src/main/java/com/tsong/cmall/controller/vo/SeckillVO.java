package com.tsong.cmall.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author Tsong
 * @Date 2023/5/10 19:35
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillVO {
    @Schema(title = "秒杀id")
    private Long seckillId;

    @Schema(title = "商品id")
    private Long goodsId;

    @Schema(title = "商品名")
    private String goodsName;

    @Schema(title = "商品封面图")
    private String goodsCoverImg;

    @Schema(title = "秒杀价格")
    private Integer seckillPrice;

    @Schema(title = "数量")
    private Integer seckillNum;

    @Schema(title = "秒杀状态")
    private Boolean seckillStatus;

    @Schema(title = "秒杀开始")
    private Date seckillBegin;

    @Schema(title = "秒杀结束")
    private Date seckillEnd;

    @Schema(title = "秒杀排序值")
    private Integer seckillRank;

    @Schema(title = "秒杀创建时间")
    private Date createTime;

    @Schema(title = "秒杀更新时间")
    private Date updateTime;
}
