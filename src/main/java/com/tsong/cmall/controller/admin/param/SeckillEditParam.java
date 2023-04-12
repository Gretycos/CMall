package com.tsong.cmall.controller.admin.param;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author Tsong
 * @Date 2023/4/12 15:12
 */
@Data
public class SeckillEditParam implements Serializable {

    @ApiModelProperty("秒杀id")
    @NotNull(message = "秒杀id不能为空")
    private Long seckillId;

    @ApiModelProperty("商品id")
    @NotNull(message = "商品id不能为空")
    private Long goodsId;

    @ApiModelProperty("秒杀价格")
    @NotNull(message = "秒杀价格不能为空")
    @Min(value = 0, message = "秒杀价格至少为0")
    private Integer seckillPrice;

    @ApiModelProperty("秒杀数量")
    @NotNull(message = "秒杀数量不能为空")
    @Min(value = 1, message = "秒杀数量至少为1")
    private Integer seckillNum;

    @ApiModelProperty("秒杀状态")
    @NotNull(message = "秒杀状态不能为空")
    @Max(value = 1, message = "不存在该状态")
    @Min(value = 0, message = "不存在该状态")
    private Boolean seckillStatus;

    @NotNull(message = "开始时间不能为空")
    private Date seckillBegin;

    @NotNull(message = "结束时间不能为空")
    private Date seckillEnd;

    @ApiModelProperty("秒杀排序值")
    @NotNull(message = "秒杀排序值不能为空")
    @Max(value = 999, message = "排序值过大")
    @Min(value = 0, message = "排序值至少为0")
    private Integer seckillRank;
}
