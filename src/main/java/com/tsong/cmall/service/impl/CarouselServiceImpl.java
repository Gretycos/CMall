package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.controller.vo.HomePageCarouselVO;
import com.tsong.cmall.dao.CarouselMapper;
import com.tsong.cmall.entity.Carousel;
import com.tsong.cmall.service.CarouselService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/3/21 18:45
 */
@Service
public class CarouselServiceImpl implements CarouselService {
    @Autowired
    private CarouselMapper carouselMapper;

    @Override
    public PageResult getCarouselPage(PageQueryUtil pageUtil) {
        List<Carousel> carousels = carouselMapper.findCarouselList(pageUtil);
        int total = carouselMapper.getTotalCarousels(pageUtil);
        return new PageResult(carousels, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String saveCarousel(Carousel carousel) {
        if (carouselMapper.insertSelective(carousel) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateCarousel(Carousel carousel) {
        Carousel temp = carouselMapper.selectByPrimaryKey(carousel.getCarouselId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        temp.setCarouselRank(carousel.getCarouselRank());
        temp.setRedirectUrl(carousel.getRedirectUrl());
        temp.setCarouselUrl(carousel.getCarouselUrl());
        temp.setUpdateTime(new Date());
        if (carouselMapper.updateByPrimaryKeySelective(temp) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public Carousel getCarouselById(Integer id) {
        return carouselMapper.selectByPrimaryKey(id);
    }

    @Override
    public Boolean deleteBatch(Long[] ids) {
        if (ids.length < 1) {
            return false;
        }
        //删除数据
        return carouselMapper.deleteBatch(ids) > 0;
    }

    @Override
    public List<HomePageCarouselVO> getCarouselsForHomePage(int number) {
        List<HomePageCarouselVO> homePageCarouselVOList = new ArrayList<>(number);
        List<Carousel> carouselList = carouselMapper.findCarouselsByNum(number);
        if (!CollectionUtils.isEmpty(carouselList)) {
            homePageCarouselVOList = BeanUtil.copyList(carouselList, HomePageCarouselVO.class);
        }
        return homePageCarouselVOList;
    }
}
