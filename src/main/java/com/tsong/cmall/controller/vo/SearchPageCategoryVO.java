package com.tsong.cmall.controller.vo;

import com.tsong.cmall.entity.GoodsCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchPageCategoryVO implements Serializable {
    private String firstLevelCategoryName;

    private List<GoodsCategory> secondLevelCategoryList;

    private String secondLevelCategoryName;

    private List<GoodsCategory> thirdLevelCategoryList;

    private String currentCategoryName;
}
