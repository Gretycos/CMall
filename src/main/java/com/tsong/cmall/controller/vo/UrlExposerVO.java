package com.tsong.cmall.controller.vo;

import com.tsong.cmall.common.SeckillStatusEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class UrlExposerVO implements Serializable {
    private static final long serialVersionUID = -7615136662052646516L;
    // 秒杀状态enum
    private SeckillStatusEnum seckillStatusEnum;
}
