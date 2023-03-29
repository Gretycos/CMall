package com.tsong.cmall.dao;

import com.tsong.cmall.entity.UserAddress;

import java.util.List;

/**
 * @Author: Tsong
 * @date: 2023/03/27/05:14
 */
public interface UserAddressMapper {
    int deleteByPrimaryKey(Long addressId);

    int insert(UserAddress row);

    int insertSelective(UserAddress row);

    UserAddress selectByPrimaryKey(Long addressId);

    int updateByPrimaryKeySelective(UserAddress row);

    int updateByPrimaryKey(UserAddress row);

    UserAddress getMyDefaultAddress(Long userId);

    List<UserAddress> findMyAddressList(Long userId);
}