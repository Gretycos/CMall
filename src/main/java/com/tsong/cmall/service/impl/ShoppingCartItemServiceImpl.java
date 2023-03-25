package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.controller.vo.ShoppingCartItemVO;
import com.tsong.cmall.dao.GoodsInfoMapper;
import com.tsong.cmall.dao.ShoppingCartItemMapper;
import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.entity.ShoppingCartItem;
import com.tsong.cmall.service.ShoppingCartItemService;
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
public class ShoppingCartItemServiceImpl implements ShoppingCartItemService {
    @Autowired
    private ShoppingCartItemMapper shoppingCartItemMapper;
    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Override
    public String saveShoppingCartItem(ShoppingCartItem shoppingCartItem) {
        ShoppingCartItem temp = shoppingCartItemMapper.selectByUserIdAndGoodsId(
                shoppingCartItem.getUserId(), shoppingCartItem.getGoodsId());
        if (temp != null) {
            //已存在则修改该记录
            temp.setGoodsCount(shoppingCartItem.getGoodsCount());
            return updateShoppingCartItem(temp);
        }
        GoodsInfo goodsInfo = goodsInfoMapper.selectByPrimaryKey(shoppingCartItem.getGoodsId());
        // 商品为空
        if (goodsInfo == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        // 超出单个商品的最大数量
        if (shoppingCartItem.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        int totalItem = shoppingCartItemMapper.selectCountByUserId(shoppingCartItem.getUserId()) + 1;
        // 购物车总数量超出最大数量
        if (totalItem > Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_TOTAL_NUMBER_ERROR.getResult();
        }
        // 保存购物车项
        if (shoppingCartItemMapper.insertSelective(shoppingCartItem) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateShoppingCartItem(ShoppingCartItem shoppingCartItem) {
        ShoppingCartItem temp = shoppingCartItemMapper.selectByPrimaryKey(shoppingCartItem.getCartItemId());
        // 数据库中不存在
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        // 超出单个商品的最大数量
        if (shoppingCartItem.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        // 数量相同不会进行修改
        if (temp.getGoodsCount().equals(shoppingCartItem.getGoodsCount())) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        // userId不同不能修改
        if (!shoppingCartItem.getUserId().equals(temp.getUserId())) {
            return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
        }
        temp.setGoodsCount(shoppingCartItem.getGoodsCount());
        temp.setUpdateTime(new Date());
        // 修改记录
        if (shoppingCartItemMapper.updateByPrimaryKeySelective(temp) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public ShoppingCartItem getShoppingCartItemById(Long shoppingCartItemId) {
        return shoppingCartItemMapper.selectByPrimaryKey(shoppingCartItemId);
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
        return shoppingCartItemVOList;
    }
}
