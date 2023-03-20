package com.tsong.cmall.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageConfig {
    private Long configId;

    private String configName;

    private Byte configType;

    private Long goodsId;

    private String redirectUrl;

    private Integer configRank;

    private Byte isDeleted;

    private Date createTime;

    private Integer createUser;

    private Date updateTime;

    private Integer updateUser;
}