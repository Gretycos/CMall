package com.tsong.cmall.controller.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HomePageCarouselVO implements Serializable {
    private String carouselUrl;

    private String redirectUrl;
}
