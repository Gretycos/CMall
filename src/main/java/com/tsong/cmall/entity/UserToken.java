package com.tsong.cmall.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Tsong
 * @date: 2023/03/27/05:14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserToken {
    private Long userId;

    private String token;

    private Date updateTime;

    private Date expireTime;
}