package com.tsong.cmall.controller.mall.param;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @Author Tsong
 * @Date 2023/4/1 17:20
 */
@Data
public class MallUserRegisterParam {
    @ApiModelProperty("登录名")
    @NotEmpty(message = "登录名不能为空")
    private String loginName;

    @ApiModelProperty("用户密码")
    @NotEmpty(message = "密码不能为空")
    private String password;
}
