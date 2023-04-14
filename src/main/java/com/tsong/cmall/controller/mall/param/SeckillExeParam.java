package com.tsong.cmall.controller.mall.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/10 15:33
 */
@Data
public class SeckillExeParam implements Serializable {
    @Schema(title = "秒杀事件id")
    @NotNull(message = "秒杀事件id不能为空")
    private Long seckillId;

    @Schema(title = "密钥")
    @NotNull(message = "密钥不能为空")
    @NotEmpty(message = "密钥不能为空")
    private String md5;

    @Schema(title = "用户id")
    @NotNull(message = "用户id不能为空")
    private Long mallUserId;
}
