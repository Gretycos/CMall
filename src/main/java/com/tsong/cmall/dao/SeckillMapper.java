package com.tsong.cmall.dao;

import com.tsong.cmall.entity.Seckill;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface SeckillMapper {
    int deleteByPrimaryKey(Long seckillId);

    int insert(Seckill row);

    int insertSelective(Seckill row);

    Seckill selectByPrimaryKey(Long seckillId);

    int updateByPrimaryKeySelective(Seckill row);

    int updateByPrimaryKey(Seckill row);
}