package com.tsong.cmall.controller.mall.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/3/28 00:19
 */
@Data
public class UserInfoUpdateParam implements Serializable {
    @Schema(title = "用户昵称")
    private String nickName;

    @Schema(title = "个性签名")
    private String introduceSign;
}
