package com.tsong.cmall.controller.mall.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/1 17:39
 */
@Data
public class MallUserPasswordParam implements Serializable {
    @ApiModelProperty("原始密码")
    private String originalPassword;

    @ApiModelProperty("新密码")
    private String newPassword;
}
