package com.tsong.cmall.controller.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MallUserVO implements Serializable {
    private Long userId;

    private String nickName;

    private String loginName;

    private String introduceSign;

    private String address;

    private Integer shopCartItemCount;
}
