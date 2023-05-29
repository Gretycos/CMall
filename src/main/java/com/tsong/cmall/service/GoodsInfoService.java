package com.tsong.cmall.service;

import com.tsong.cmall.controller.vo.GoodsNameVO;
import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;

import java.util.List;

public interface GoodsInfoService {
    PageResult getGoodsInfoPage(PageQueryUtil pageUtil);

    /**
     * @Description 添加商品
     * @Param [goods]
     * @Return java.lang.String
     */
    String saveGoodsInfo(GoodsInfo goods);

    /**
     * @Description 批量新增商品数目
     * @Param [goodsInfoList]
     * @Return void
     */
    void batchSaveGoodsInfo(List<GoodsInfo> goodsInfoList);

    /**
     * @Description 修改商品信息
     * @Param [goods]
     * @Return java.lang.String
     */
    String updateGoodsInfo(GoodsInfo goods);

    /**
     * @Description 获取商品详情，管理员
     * @Param [id, createUser]
     * @Return com.tsong.cmall.entity.GoodsInfo
     */
    GoodsInfo getGoodsInfoById(Long id, Long createUser);

    /**
     * @Description 获取商品详情
     * @Param [id]
     * @Return com.tsong.cmall.entity.GoodsInfo
     */
    GoodsInfo getGoodsInfoById(Long id);

    /**
     * @Description 批量修改销售状态（上架/下架）
     * @Param [ids, saleStatus]
     * @Return java.lang.Boolean
     */
    Boolean batchUpdateSaleStatus(Long[] ids, int saleStatus, Long createUser);

    /**
     * @Description 商品搜索
     * @Param [pageUtil]
     * @Return com.tsong.cmall.util.PageResult
     */
    PageResult searchGoodsInfo(PageQueryUtil pageUtil);

    /**
     * @Description 管理员的所有商品
     * @Param [createUser]
     * @Return java.util.List<com.tsong.cmall.controller.vo.GoodsNameVO>
     */
    List<GoodsNameVO> getAllGoods(Long createUser);
}
