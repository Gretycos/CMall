package com.tsong.cmall.service;

import com.tsong.cmall.controller.vo.HomePageCarouselVO;
import com.tsong.cmall.entity.Carousel;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;

import java.util.List;

public interface CarouselService {
    PageResult getCarouselPage(PageQueryUtil pageUtil);

    String saveCarousel(Carousel carousel);

    String updateCarousel(Carousel carousel);

    Carousel getCarouselById(Integer id);

    Boolean deleteBatch(Long[] ids);

    /**
     * @Description 返回固定数量的轮播图对象(首页调用)
     * @Param [number]
     * @Return java.util.List<com.tsong.cmall.controller.vo.HomePageCarouselVO>
     */
    List<HomePageCarouselVO> getCarouselsForHomePage(int number);
}
