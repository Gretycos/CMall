package com.tsong.cmall.controller.vo;

import com.tsong.cmall.entity.GoodsCategory;
import com.tsong.cmall.entity.GoodsInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/4/3 16:43
 */
@Data
public class GoodsAndCategoryVO implements Serializable {
    @ApiModelProperty("商品")
    private GoodsInfo goodsInfo;

    @ApiModelProperty("一级分类")
    private GoodsCategory firstCategory;

    @ApiModelProperty("二级分类")
    private GoodsCategory secondCategory;

    @ApiModelProperty("三级分类")
    private GoodsCategory thirdCategory;
}
