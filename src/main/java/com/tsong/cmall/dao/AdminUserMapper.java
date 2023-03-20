package com.tsong.cmall.dao;

import com.tsong.cmall.entity.AdminUser;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface AdminUserMapper {
    int deleteByPrimaryKey(Integer adminUserId);

    int insert(AdminUser row);

    int insertSelective(AdminUser row);

    AdminUser selectByPrimaryKey(Integer adminUserId);

    int updateByPrimaryKeySelective(AdminUser row);

    int updateByPrimaryKey(AdminUser row);
}