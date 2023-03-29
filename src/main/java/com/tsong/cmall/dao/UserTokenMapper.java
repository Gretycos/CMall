package com.tsong.cmall.dao;

import com.tsong.cmall.entity.UserToken;

/**
 * @Author: Tsong
 * @date: 2023/03/27/05:14
 */
public interface UserTokenMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(UserToken row);

    int insertSelective(UserToken row);

    UserToken selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(UserToken row);

    int updateByPrimaryKey(UserToken row);
}