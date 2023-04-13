package com.tsong.cmall.controller.admin.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Tsong
 * @Date 2023/4/3 13:55
 */
@Data
public class BatchIdParam implements Serializable {
    @Schema(title = "id数组")
    Long[] ids;
}
