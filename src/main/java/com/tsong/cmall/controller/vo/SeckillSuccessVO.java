package com.tsong.cmall.controller.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SeckillSuccessVO implements Serializable {
    private static final long serialVersionUID = 1503814153626594835L;

    private Long seckillSuccessId;

    private String md5;
}
