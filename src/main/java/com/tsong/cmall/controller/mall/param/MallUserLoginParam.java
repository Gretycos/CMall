package com.tsong.cmall.controller.mall.param;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/1 17:15
 */
@Data
public class MallUserLoginParam implements Serializable {
    @ApiModelProperty("登录名")
    @NotEmpty(message = "登录名不能为空")
    private String loginName;

    @ApiModelProperty("用户密码(需要MD5加密)")
    @NotEmpty(message = "密码不能为空")
    private String passwordMd5;
}
