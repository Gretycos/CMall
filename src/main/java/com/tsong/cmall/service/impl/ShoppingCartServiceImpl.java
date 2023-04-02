package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.controller.mall.param.SaveCartItemParam;
import com.tsong.cmall.controller.mall.param.UpdateCartItemParam;
import com.tsong.cmall.controller.vo.ShoppingCartItemVO;
import com.tsong.cmall.dao.GoodsInfoMapper;
import com.tsong.cmall.dao.ShoppingCartItemMapper;
import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.entity.ShoppingCartItem;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.ShoppingCartService;
import com.tsong.cmall.util.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author Tsong
 * @Date 2023/3/25 17:00
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartItemMapper shoppingCartItemMapper;
    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Override
    public String saveShoppingCartItem(SaveCartItemParam saveCartItemParam, Long userId) {
        ShoppingCartItem temp = shoppingCartItemMapper.selectByUserIdAndGoodsId(
                userId, saveCartItemParam.getGoodsId());
        // 已存在该购物车项目
        if (temp != null) {
            UpdateCartItemParam updateCartItemParam = new UpdateCartItemParam();
            updateCartItemParam.setCartItemId(saveCartItemParam.getGoodsId());
            // 叠加数量
            updateCartItemParam.setGoodsCount(temp.getGoodsCount() + saveCartItemParam.getGoodsCount());
            return updateShoppingCartItem(updateCartItemParam, userId);
        }

        // 不存在该购物车项目
        // 查找物品
        GoodsInfo goodsInfo = goodsInfoMapper.selectByPrimaryKey(saveCartItemParam.getGoodsId());
        // 商品为空
        if (goodsInfo == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }

        // 超出单个商品的最大数量
        if (saveCartItemParam.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }

        int totalItem = shoppingCartItemMapper.selectCountByUserId(userId) + 1;
        // 购物车总数量超出最大数量
        if (totalItem > Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_TOTAL_NUMBER_ERROR.getResult();
        }

        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        BeanUtil.copyProperties(saveCartItemParam, shoppingCartItem);
        shoppingCartItem.setUserId(userId);
        // 保存购物车项
        if (shoppingCartItemMapper.insertSelective(shoppingCartItem) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateShoppingCartItem(UpdateCartItemParam updateCartItemParam, Long userId) {
        ShoppingCartItem temp = shoppingCartItemMapper.selectByPrimaryKey(updateCartItemParam.getCartItemId());
        // 数据库中不存在
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        // 超出单个商品的最大数量
        if (updateCartItemParam.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        // 数量相同不会进行修改
        if (temp.getGoodsCount().equals(updateCartItemParam.getGoodsCount())) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        // userId不同不能修改
        if (!userId.equals(temp.getUserId())) {
            return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
        }
        temp.setGoodsCount(updateCartItemParam.getGoodsCount());
        temp.setUpdateTime(new Date());
        // 修改记录
        if (shoppingCartItemMapper.updateByPrimaryKeySelective(temp) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public ShoppingCartItem getShoppingCartItemById(Long shoppingCartItemId) {
        ShoppingCartItem shoppingCartItem = shoppingCartItemMapper.selectByPrimaryKey(shoppingCartItemId);
        if (shoppingCartItem == null){
            CMallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        return shoppingCartItem;
    }

    @Override
    public List<ShoppingCartItemVO> getCartItemsForConfirmPage(List<Long> cartItemIds, Long userId) {
        List<ShoppingCartItemVO> shoppingCartItemVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(cartItemIds)) {
            CMallException.fail("购物项不能为空");
        }
        List<ShoppingCartItem> shoppingCartItemList = shoppingCartItemMapper
                .selectByUserIdAndCartItemIds(userId, cartItemIds);
        transformToVO(shoppingCartItemList,shoppingCartItemVOList);
        return shoppingCartItemVOList;
    }

    @Override
    public Boolean deleteById(Long shoppingCartItemId, Long userId) {
        ShoppingCartItem shoppingCartItem = shoppingCartItemMapper.selectByPrimaryKey(shoppingCartItemId);
        if (shoppingCartItem == null){
            return false;
        }
        // userId不同不能删除
        if (!userId.equals(shoppingCartItem.getUserId())){
            return false;
        }
        return shoppingCartItemMapper.deleteByPrimaryKey(shoppingCartItemId) > 0;
    }

    @Override
    public List<ShoppingCartItemVO> getMyShoppingCartItems(Long mallUserId) {
        List<ShoppingCartItemVO> shoppingCartItemVOList = new ArrayList<>();
        List<ShoppingCartItem> shoppingCartItemList = shoppingCartItemMapper.selectByUserId(
                mallUserId, Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER);
        transformToVO(shoppingCartItemList, shoppingCartItemVOList);
        return shoppingCartItemVOList;
    }

    private void transformToVO(List<ShoppingCartItem> shoppingCartItemList, List<ShoppingCartItemVO> shoppingCartItemVOList){
        if (!CollectionUtils.isEmpty(shoppingCartItemList)) {
            // 商品id表
            List<Long> goodsIds = shoppingCartItemList.stream()
                    .map(ShoppingCartItem::getGoodsId).collect(Collectors.toList());
            // 商品表
            List<GoodsInfo> goodsList = goodsInfoMapper.selectByPrimaryKeys(goodsIds);
            // id -> 商品
            Map<Long, GoodsInfo> goodsMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(goodsList)) {
                goodsMap = goodsList.stream()
                        .collect(Collectors.toMap(GoodsInfo::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
            }
            // 实体模型 -> 视图模型
            for (ShoppingCartItem shoppingCartItem : shoppingCartItemList) {
                ShoppingCartItemVO shoppingCartItemVO = new ShoppingCartItemVO();
                BeanUtil.copyProperties(shoppingCartItem, shoppingCartItemVO);
                // 从实体模型获得商品，把商品的信息填入视图模型
                GoodsInfo goods = goodsMap.get(shoppingCartItem.getGoodsId());
                if (goods != null){
                    shoppingCartItemVO.setGoodsCoverImg(goods.getGoodsCoverImg());
                    String goodsName = goods.getGoodsName();
                    // 字符串过长导致文字超出的问题
                    if (goodsName.length() > 28) {
                        goodsName = goodsName.substring(0, 28) + "...";
                    }
                    shoppingCartItemVO.setGoodsName(goodsName);
                    shoppingCartItemVO.setSellingPrice(goods.getSellingPrice());
                    shoppingCartItemVOList.add(shoppingCartItemVO);
                }
            }
        }
    }
}
