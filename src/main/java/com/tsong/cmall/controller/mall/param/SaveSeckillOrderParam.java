package com.tsong.cmall.controller.mall.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/7 00:19
 */
@Data
public class SaveSeckillOrderParam implements Serializable {
    @ApiModelProperty("秒杀成功id")
    private Long seckillSuccessId;

    @ApiModelProperty("秒杀密钥")
    private String seckillSecretKey;

    @ApiModelProperty("地址id")
    private Long addressId;
}
