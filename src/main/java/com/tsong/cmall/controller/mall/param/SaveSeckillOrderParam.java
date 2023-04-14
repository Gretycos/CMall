package com.tsong.cmall.controller.mall.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/7 00:19
 */
@Data
public class SaveSeckillOrderParam implements Serializable {
    @Schema(title = "秒杀成功id")
    @NotNull(message = "秒杀id不能为空")
    private Long seckillSuccessId;

    @Schema(title = "秒杀密钥")
    @NotNull(message = "秒杀密钥不能为空")
    private String seckillSecretKey;

    @Schema(title = "地址id")
    @NotNull(message = "地址不能为空")
    private Long addressId;
}
