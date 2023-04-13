package com.tsong.cmall.controller.mall.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/3/30 21:42
 */
@Data
public class SaveCartItemParam implements Serializable {

    @Schema(title = "商品数量")
    private Integer goodsCount;

    @Schema(title = "商品id")
    private Long goodsId;
}
