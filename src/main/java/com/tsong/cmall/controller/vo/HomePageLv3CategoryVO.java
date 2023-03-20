package com.tsong.cmall.controller.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HomePageLv3CategoryVO implements Serializable {
    private Long categoryId;

    private Byte categoryLevel;

    private String categoryName;
}
