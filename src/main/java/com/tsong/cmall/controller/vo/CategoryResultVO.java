package com.tsong.cmall.controller.vo;

import com.tsong.cmall.entity.GoodsCategory;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/4/3 14:30
 */
@Data
public class CategoryResultVO implements Serializable {
    @ApiModelProperty("二级分类列表")
    private List<GoodsCategory> secondLevelCategories;
    @ApiModelProperty("三级分类列表")
    private List<GoodsCategory> thirdLevelCategories;
}
