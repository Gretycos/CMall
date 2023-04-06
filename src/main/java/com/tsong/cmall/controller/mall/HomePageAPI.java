package com.tsong.cmall.controller.mall;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.HomePageConfigTypeEnum;
import com.tsong.cmall.controller.vo.HomePageCarouselVO;
import com.tsong.cmall.controller.vo.HomePageConfigGoodsVO;
import com.tsong.cmall.controller.vo.HomePageInfoVO;
import com.tsong.cmall.service.CarouselService;
import com.tsong.cmall.service.HomePageConfigService;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/3/31 22:55
 */
@RestController
@Api(value = "homepage", tags = "1-1.商城首页接口")
@RequestMapping("/api")
public class HomePageAPI {

    @Autowired
    private CarouselService carouselService;
    @Autowired
    private HomePageConfigService homePageConfigService;

    @GetMapping("/index-infos")
    @ApiOperation(value = "获取首页数据", notes = "轮播图、新品、推荐等")
    public Result<HomePageInfoVO> indexInfo() {
        HomePageInfoVO homePageInfoVO = new HomePageInfoVO();
        List<HomePageCarouselVO> carousels = carouselService
                .getCarouselsForHomePage(Constants.HOME_PAGE_CAROUSEL_NUMBER);
        List<HomePageConfigGoodsVO> hotGoodsList = homePageConfigService.getConfigGoodsForHomePage(
                        HomePageConfigTypeEnum.HOME_PAGE_GOODS_HOT.getType(),
                        Constants.HOME_PAGE_GOODS_HOT_NUMBER);
        List<HomePageConfigGoodsVO> newGoodsList = homePageConfigService.getConfigGoodsForHomePage(
                HomePageConfigTypeEnum.HOME_PAGE_GOODS_NEW.getType(),
                Constants.HOME_PAGE_GOODS_NEW_NUMBER);
        List<HomePageConfigGoodsVO> recommendGoodsList = homePageConfigService.getConfigGoodsForHomePage(
                HomePageConfigTypeEnum.HOME_PAGE_GOODS_RECOMMENDED.getType(),
                Constants.HOME_PAGE_GOODS_RECOMMENDED_NUMBER);
        homePageInfoVO.setCarousels(carousels);
        homePageInfoVO.setHotGoodsList(hotGoodsList);
        homePageInfoVO.setNewGoodsList(newGoodsList);
        homePageInfoVO.setRecommendGoodsList(recommendGoodsList);
        return ResultGenerator.genSuccessResult(homePageInfoVO);
    }
}
