package com.tsong.cmall.service;

import com.tsong.cmall.controller.vo.ShoppingCartItemVO;
import com.tsong.cmall.entity.ShoppingCartItem;

import java.util.List;

public interface ShoppingCartItemService {
    /**
     * @Description 保存商品至购物车中
     * @Param [shoppingCartItem]
     * @Return java.lang.String
     */
    String saveShoppingCartItem(ShoppingCartItem shoppingCartItem);

    /**
     * @Description 修改购物车中的属性
     * @Param [shoppingCartItem]
     * @Return java.lang.String
     */
    String updateShoppingCartItem(ShoppingCartItem shoppingCartItem);

    /**
     * @Description 获取购物项详情
     * @Param [shoppingCartItemId]
     * @Return com.tsong.cmall.entity.ShoppingCartItem
     */
    ShoppingCartItem getShoppingCartItemById(Long shoppingCartItemId);

    /**
     * @Description 删除购物车中的商品
     * @Param [shoppingCartItemId, userId]
     * @Return java.lang.Boolean
     */
    Boolean deleteById(Long shoppingCartItemId, Long userId);

    /**
     * @Description 获取我的购物车中的列表数据
     * @Param [mallUserId]
     * @Return java.util.List<com.tsong.cmall.controller.vo.ShoppingCartItemVO>
     */
    List<ShoppingCartItemVO> getMyShoppingCartItems(Long mallUserId);
}
