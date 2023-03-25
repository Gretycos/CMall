package com.tsong.cmall.controller.vo;

import com.tsong.cmall.common.SeckillStatusEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class UrlExposerVO implements Serializable {
    private static final long serialVersionUID = -7615136662052646516L;
    // 秒杀状态enum
    private SeckillStatusEnum seckillStatusEnum;

    // 一种加密措施
    private String md5;

    // id
    private long seckillId;

    // 系统当前时间（毫秒）
    private long now;

    // 开启时间
    private long start;

    // 结束时间
    private long end;

    public UrlExposerVO(SeckillStatusEnum seckillStatusEnum, String md5, long seckillId) {
        this.seckillStatusEnum = seckillStatusEnum;
        this.md5 = md5;
        this.seckillId = seckillId;
    }

    public UrlExposerVO(SeckillStatusEnum seckillStatusEnum, long seckillId, long now, long start, long end) {
        this.seckillStatusEnum = seckillStatusEnum;
        this.seckillId = seckillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    public UrlExposerVO(SeckillStatusEnum seckillStatusEnum, long seckillId) {
        this.seckillStatusEnum = seckillStatusEnum;
        this.seckillId = seckillId;
    }
}
