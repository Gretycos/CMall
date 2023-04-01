package com.tsong.cmall.controller.mall.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/1 17:34
 */
@Data
public class MallUserUpdateParam implements Serializable {
    @ApiModelProperty("用户昵称")
    private String nickName;

    @ApiModelProperty("个性签名")
    private String introduceSign;
}
