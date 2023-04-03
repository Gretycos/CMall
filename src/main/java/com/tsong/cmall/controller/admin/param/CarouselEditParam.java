package com.tsong.cmall.controller.admin.param;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author Tsong
 * @Date 2023/4/3 13:54
 */
@Data
public class CarouselEditParam {
    @ApiModelProperty("待修改轮播图id")
    @NotNull(message = "轮播图id不能为空")
    @Min(1)
    private Integer carouselId;

    @ApiModelProperty("轮播图URL地址")
    @NotEmpty(message = "轮播图URL不能为空")
    private String carouselUrl;

    @ApiModelProperty("轮播图跳转地址")
    @NotEmpty(message = "轮播图跳转地址不能为空")
    private String redirectUrl;

    @ApiModelProperty("排序值")
    @Min(value = 1, message = "carouselRank最低为1")
    @Max(value = 200, message = "carouselRank最高为200")
    @NotNull(message = "carouselRank不能为空")
    private Integer carouselRank;
}
