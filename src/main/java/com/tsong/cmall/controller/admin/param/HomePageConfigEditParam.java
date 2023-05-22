package com.tsong.cmall.controller.admin.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/3 17:12
 */
@Data
public class HomePageConfigEditParam implements Serializable {
    @Schema(title = "待修改配置id")
    @NotNull(message = "configId不能为空")
    @Min(value = 1, message = "configId不能为空")
    private Long configId;

    @Schema(title = "配置的名称")
    @NotEmpty(message = "configName不能为空")
    private String configName;

    @Schema(title = "配置类别")
    @NotNull(message = "configType不能为空")
    @Min(value = 1, message = "configType最小为1")
    @Max(value = 5, message = "configType最大为5")
    private Byte configType;

    @Schema(title = "商品id")
    @NotNull(message = "商品id不能为空")
    @Min(value = 1, message = "商品id不能为空")
    private Long goodsId;

    @Schema(title = "配置项链接")
    private String redirectUrl;

    @Schema(title = "排序值")
    @Min(value = 1, message = "configRank最低为1")
    @Max(value = 200, message = "configRank最高为200")
    @NotNull(message = "configRank不能为空")
    private Integer configRank;
}
