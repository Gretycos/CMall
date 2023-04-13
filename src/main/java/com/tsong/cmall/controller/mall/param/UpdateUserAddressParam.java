package com.tsong.cmall.controller.mall.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/2 23:58
 */
@Data
public class UpdateUserAddressParam implements Serializable {
    @Schema(title = "地址id")
    private Long addressId;

    @Schema(title = "用户id")
    private Long userId;

    @Schema(title = "收件人名称")
    private String userName;

    @Schema(title = "收件人联系方式")
    private String userPhone;

    @Schema(title = "是否默认地址 0-不是 1-是")
    private Byte defaultFlag;

    @Schema(title = "省")
    private String provinceName;

    @Schema(title = "市")
    private String cityName;

    @Schema(title = "区/县")
    private String regionName;

    @Schema(title = "详细地址")
    private String detailAddress;
}
