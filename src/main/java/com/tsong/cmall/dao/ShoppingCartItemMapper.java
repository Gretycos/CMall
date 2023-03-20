package com.tsong.cmall.dao;

import com.tsong.cmall.entity.ShoppingCartItem;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface ShoppingCartItemMapper {
    int deleteByPrimaryKey(Long cartItemId);

    int insert(ShoppingCartItem row);

    int insertSelective(ShoppingCartItem row);

    ShoppingCartItem selectByPrimaryKey(Long cartItemId);

    int updateByPrimaryKeySelective(ShoppingCartItem row);

    int updateByPrimaryKey(ShoppingCartItem row);
}