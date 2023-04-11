package com.tsong.cmall.controller.admin.param;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author Tsong
 * @Date 2023/4/11 14:17
 */
@Data
public class CouponAddParam implements Serializable {
    @ApiModelProperty("优惠券名称")
    @NotEmpty(message = "优惠券名称不能为空")
    @Length(max = 32,message = "商品名称内容过长")
    private String couponName;

    @ApiModelProperty("优惠券简介")
    @NotEmpty(message = "优惠券简介不能为空")
    @Length(max = 200,message = "优惠券简介内容过长")
    private String couponDesc;

    @ApiModelProperty("优惠券数量")
    @NotNull(message = "优惠券数量不能为空")
    @Min(value = 0, message = "优惠券数量至少为0")
    private Integer couponTotal;

    @ApiModelProperty("优惠金额")
    @NotNull(message = "优惠金额不能为空")
    @Min(value = 0, message = "优惠金额至少为0")
    @Max(value = 10000, message = "优惠金额最多为10000")
    private Integer discount;

    @ApiModelProperty("最少消费金额")
    @NotNull(message = "最少消费金额不能为空")
    @Min(value = 0, message = "最少消费金额至少为0")
    @Max(value = 1000000, message = "优惠金额最多为10000")
    private Integer min;

    @ApiModelProperty("领券限制数量")
    @NotNull(message = "领券限制数量不能为空")
    @Max(value = 1, message = "超出有限制的领取数量")
    @Min(value = 0, message = "领取数量最少是0")
    private Byte couponLimit;

    @ApiModelProperty("优惠券类型")
    @NotNull(message = "优惠券类型不能为空")
    @Max(value = 2, message = "不存在该分类")
    @Min(value = 0, message = "不存在该分类")
    private Byte couponType;

    @ApiModelProperty("优惠券状态")
    @NotNull(message = "优惠券状态不能为空")
    @Max(value = 1, message = "不存在该状态")
    @Min(value = 0, message = "不存在该状态")
    private Byte couponStatus;

    @ApiModelProperty("可用商品类型")
    @NotNull(message = "可用商品类型不能为空")
    @Max(value = 2, message = "不存在该分类")
    @Min(value = 0, message = "不存在该分类")
    private Byte goodsType;

    @ApiModelProperty("可用商品范围")
    @NotEmpty(message = "可用商品范围不能为空")
    private String goodsValue;

    @NotNull(message = "开始时间不能为空")
    private Date couponStartTime;

    @NotNull(message = "结束时间不能为空")
    private Date couponEndTime;
}
