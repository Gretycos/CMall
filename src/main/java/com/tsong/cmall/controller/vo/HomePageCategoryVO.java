package com.tsong.cmall.controller.vo;

import java.io.Serializable;
import java.util.List;

public class HomePageCategoryVO implements Serializable {
    private Long categoryId;

    private Byte categoryLevel;

    private String categoryName;

    private List<HomePageLv2CategoryVO> homePageLv2CategoryVOList;
}
