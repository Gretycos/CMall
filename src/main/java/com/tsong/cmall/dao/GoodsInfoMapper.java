package com.tsong.cmall.dao;

import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.entity.StockNumDTO;
import com.tsong.cmall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

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

    GoodsInfo selectByIdAndCreateUser(Long goodsId, Long createUser);

    List<GoodsInfo> selectByCreateUser(Long createUser);

    List<GoodsInfo> selectByPrimaryKeys(List<Long> goodsIds);

    List<GoodsInfo> findGoodsListBySearch(PageQueryUtil pageUtil);

    int getTotalGoodsBySearch(PageQueryUtil pageUtil);

    int batchInsert(List<GoodsInfo> goodsList);

    int updateStockNum(List<StockNumDTO> stockNumDTOS);

    int recoverStockNum(List<StockNumDTO> stockNumDTOS);

    int batchUpdateSaleStatus(Long[] orderIds, int saleStatus, Long createUser);

    boolean addStock(Long goodsId, Integer goodsCount);
}