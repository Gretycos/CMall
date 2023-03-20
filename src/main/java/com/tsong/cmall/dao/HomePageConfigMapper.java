package com.tsong.cmall.dao;

import com.tsong.cmall.entity.HomePageConfig;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface HomePageConfigMapper {
    int deleteByPrimaryKey(Long configId);

    int insert(HomePageConfig row);

    int insertSelective(HomePageConfig row);

    HomePageConfig selectByPrimaryKey(Long configId);

    int updateByPrimaryKeySelective(HomePageConfig row);

    int updateByPrimaryKey(HomePageConfig row);
}