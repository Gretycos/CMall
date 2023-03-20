package com.tsong.cmall.controller.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HomePageLv2CategoryVO implements Serializable {
    private Long categoryId;

    private Long parentId;

    private Byte categoryLevel;

    private String categoryName;

    private List<HomePageLv3CategoryVO> homePageLv3CategoryVOList;
}
