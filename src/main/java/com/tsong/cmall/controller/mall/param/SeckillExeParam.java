package com.tsong.cmall.controller.mall.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/10 15:33
 */
@Data
public class SeckillExeParam implements Serializable {
    @ApiModelProperty("秒杀事件id")
    private Long seckillId;

    @ApiModelProperty("密钥")
    private String md5;

    @ApiModelProperty("用户id")
    private Long mallUserId;
}
