package com.tsong.cmall.dao;

import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.entity.StockNumDTO;
import com.tsong.cmall.util.PageQueryUtil;

import java.util.List;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface GoodsInfoMapper {
    int deleteByPrimaryKey(Long goodsId);

    int insert(GoodsInfo row);

    int insertSelective(GoodsInfo row);

    GoodsInfo selectByPrimaryKey(Long goodsId);

    int updateByPrimaryKeySelective(GoodsInfo row);

    int updateByPrimaryKeyWithBLOBs(GoodsInfo row);

    int updateByPrimaryKey(GoodsInfo row);

    GoodsInfo selectByCategoryIdAndName(String goodsName, Long goodsCategoryId);

    List<GoodsInfo> findGoodsList(PageQueryUtil pageUtil);

    int getTotalGoods(PageQueryUtil pageUtil);

    List<GoodsInfo> selectByPrimaryKeys(List<Long> goodsIds);

    List<GoodsInfo> findGoodsListBySearch(PageQueryUtil pageUtil);

    int getTotalGoodsBySearch(PageQueryUtil pageUtil);

    int batchInsert(List<GoodsInfo> goodsList);

    int updateStockNum(List<StockNumDTO> stockNumDTOS);

    int batchUpdateSaleStatus(Long[] orderIds, int saleStatus);

    boolean addStock(Long goodsId, Integer goodsCount);
}