package com.tsong.cmall.dao;

import com.tsong.cmall.entity.Carousel;
import com.tsong.cmall.util.PageQueryUtil;

import java.util.List;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface CarouselMapper {
    int deleteByPrimaryKey(Integer carouselId);

    int insert(Carousel row);

    int insertSelective(Carousel row);

    Carousel selectByPrimaryKey(Integer carouselId);

    int updateByPrimaryKeySelective(Carousel row);

    int updateByPrimaryKey(Carousel row);

    List<Carousel> findCarouselList(PageQueryUtil pageUtil);

    int getTotalCarousels(PageQueryUtil pageUtil);

    List<Carousel> findCarouselsByNum(int number);

    int deleteBatch(Integer[] ids);
}