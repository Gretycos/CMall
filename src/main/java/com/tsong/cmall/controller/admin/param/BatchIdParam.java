package com.tsong.cmall.controller.admin.param;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/3 13:55
 */
@Data
public class BatchIdParam implements Serializable {
    // id数组
    Long[] ids;
}
