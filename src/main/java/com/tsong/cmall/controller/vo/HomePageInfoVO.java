package com.tsong.cmall.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/3/31 22:57
 */
@Data
public class HomePageInfoVO implements Serializable {
    @ApiModelProperty("轮播图(列表)")
    private List<HomePageCarouselVO> carousels;

    @ApiModelProperty("首页热销商品(列表)")
    private List<HomePageConfigGoodsVO> hotGoodsList;

    @ApiModelProperty("首页新品推荐(列表)")
    private List<HomePageConfigGoodsVO> newGoodsList;

    @ApiModelProperty("首页推荐商品(列表)")
    private List<HomePageConfigGoodsVO> recommendGoodsList;
}
