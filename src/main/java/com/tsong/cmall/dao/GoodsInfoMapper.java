package com.tsong.cmall.dao;

import com.tsong.cmall.entity.GoodsInfo;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface GoodsInfoMapper {
    int deleteByPrimaryKey(Long goodsId);

    int insert(GoodsInfo row);

    int insertSelective(GoodsInfo row);

    GoodsInfo selectByPrimaryKey(Long goodsId);

    int updateByPrimaryKeySelective(GoodsInfo row);

    int updateByPrimaryKeyWithBLOBs(GoodsInfo row);

    int updateByPrimaryKey(GoodsInfo row);
}