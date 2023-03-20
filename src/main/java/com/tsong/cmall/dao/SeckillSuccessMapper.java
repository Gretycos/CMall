package com.tsong.cmall.dao;

import com.tsong.cmall.entity.SeckillSuccess;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface SeckillSuccessMapper {
    int deleteByPrimaryKey(Long secId);

    int insert(SeckillSuccess row);

    int insertSelective(SeckillSuccess row);

    SeckillSuccess selectByPrimaryKey(Long secId);

    int updateByPrimaryKeySelective(SeckillSuccess row);

    int updateByPrimaryKey(SeckillSuccess row);
}