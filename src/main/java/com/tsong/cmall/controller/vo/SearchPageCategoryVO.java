package com.tsong.cmall.controller.vo;

import com.tsong.cmall.entity.GoodsCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchPageCategoryVO implements Serializable {
    @Schema(title = "一级分类名")
    private String firstLevelCategoryName;

    @Schema(title = "二级分类列表")
    private List<GoodsCategory> secondLevelCategoryList;

    @Schema(title = "二级分类名")
    private String secondLevelCategoryName;

    @Schema(title = "三级分类列表")
    private List<GoodsCategory> thirdLevelCategoryList;

    @Schema(title = "当前分类名")
    private String currentCategoryName;
}
