package com.tsong.cmall.controller.mall.param;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/1 17:34
 */
@Data
public class MallUserUpdateParam implements Serializable {
    @ApiModelProperty("用户昵称")
    @NotEmpty(message = "昵称不能为空")
    @Length(max = 16,message = "昵称过长")
    private String nickName;

    @ApiModelProperty("个性签名")
    @NotEmpty(message = "个性签名不能为空")
    @Length(max = 140,message = "个性签名过长")
    private String introduceSign;
}
