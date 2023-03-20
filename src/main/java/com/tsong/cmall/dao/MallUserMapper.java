package com.tsong.cmall.dao;

import com.tsong.cmall.entity.MallUser;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface MallUserMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(MallUser row);

    int insertSelective(MallUser row);

    MallUser selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(MallUser row);

    int updateByPrimaryKey(MallUser row);
}