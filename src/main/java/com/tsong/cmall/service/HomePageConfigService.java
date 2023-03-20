package com.tsong.cmall.service;

import com.tsong.cmall.controller.vo.HomePageConfigGoodsVO;
import com.tsong.cmall.entity.HomePageConfig;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;

import java.util.List;

public interface HomePageConfigService {
    PageResult getConfigsPage(PageQueryUtil pageUtil);

    String saveHomePageConfig(HomePageConfig homePageConfig);

    String updateHomePageConfig(HomePageConfig homePageConfig);

    HomePageConfig getHomePageConfigById(Long id);

    /**
     * @Description 返回固定数量的首页配置商品对象(首页调用)
     * @Param [configType, number]
     * @Return java.util.List<HomePageConfigGoodsVO>
     */

    List<HomePageConfigGoodsVO> getConfigGoodsForHomePage(int configType, int number);

    Boolean deleteBatch(Long[] ids);
}
