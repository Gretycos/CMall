package com.tsong.cmall.controller.mall.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/7 00:19
 */
@Data
public class SaveSeckillOrderParam implements Serializable {
    @Schema(title = "秒杀成功id")
    private Long seckillSuccessId;

    @Schema(title = "秒杀密钥")
    private String seckillSecretKey;

    @Schema(title = "地址id")
    private Long addressId;
}
