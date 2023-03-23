package com.tsong.cmall.controller.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HomePageCategoryVO implements Serializable {
    private Long categoryId;

    private Byte categoryLevel;

    private String categoryName;

    private List<SecondLevelCategoryVO> secondLevelCategoryVOList;
}
