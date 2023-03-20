package com.tsong.cmall.dao;

import com.tsong.cmall.entity.GoodsCategory;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface GoodsCategoryMapper {
    int deleteByPrimaryKey(Long categoryId);

    int insert(GoodsCategory row);

    int insertSelective(GoodsCategory row);

    GoodsCategory selectByPrimaryKey(Long categoryId);

    int updateByPrimaryKeySelective(GoodsCategory row);

    int updateByPrimaryKey(GoodsCategory row);
}