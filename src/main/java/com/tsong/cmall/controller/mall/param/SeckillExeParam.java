package com.tsong.cmall.controller.mall.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/10 15:33
 */
@Data
public class SeckillExeParam implements Serializable {
    @Schema(title = "秒杀事件id")
    private Long seckillId;

    @Schema(title = "密钥")
    private String md5;

    @Schema(title = "用户id")
    private Long mallUserId;
}
