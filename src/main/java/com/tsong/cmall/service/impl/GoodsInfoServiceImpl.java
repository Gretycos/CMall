package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.CategoryLevelEnum;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.controller.vo.GoodsNameVO;
import com.tsong.cmall.controller.vo.SearchPageGoodsVO;
import com.tsong.cmall.dao.GoodsCategoryMapper;
import com.tsong.cmall.dao.GoodsInfoMapper;
import com.tsong.cmall.entity.GoodsCategory;
import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.GoodsInfoService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/3/23 23:20
 */
@Service
public class GoodsInfoServiceImpl implements GoodsInfoService {
    @Autowired
    private GoodsInfoMapper goodsInfoMapper;
    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public PageResult getGoodsInfoPage(PageQueryUtil pageUtil) {
        List<GoodsInfo> goodsList = goodsInfoMapper.findGoodsList(pageUtil);
        int total = goodsInfoMapper.getTotalGoods(pageUtil);
        return new PageResult(goodsList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String saveGoodsInfo(GoodsInfo goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (goodsCategory == null
                || goodsCategory.getCategoryLevel().intValue() != CategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        // 查看是否已存在物品
        if (goodsInfoMapper.selectByCategoryIdAndName(goods.getGoodsName(), goods.getGoodsCategoryId()) != null) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        // 插入
        if (goodsInfoMapper.insertSelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public void batchSaveGoodsInfo(List<GoodsInfo> goodsInfoList) {
        if (!CollectionUtils.isEmpty(goodsInfoList)) {
            goodsInfoMapper.batchInsert(goodsInfoList);
        }
    }

    @Override
    public String updateGoodsInfo(GoodsInfo goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (goodsCategory == null
                || goodsCategory.getCategoryLevel().intValue() != CategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        GoodsInfo temp = goodsInfoMapper.selectByPrimaryKey(goods.getGoodsId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        GoodsInfo temp2 = goodsInfoMapper.selectByCategoryIdAndName(
                goods.getGoodsName(), goods.getGoodsCategoryId());
        if (temp2 != null && !temp2.getGoodsId().equals(goods.getGoodsId())) {
            // name和分类id相同且不同id 不能继续修改
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        goods.setUpdateTime(new Date());
        if (goodsInfoMapper.updateByPrimaryKeySelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public GoodsInfo getGoodsInfoById(Long id, Long createUser) {
        GoodsInfo goodsInfo = goodsInfoMapper.selectByIdAndCreateUser(id, createUser);
        if (goodsInfo == null) {
            CMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        return goodsInfo;
    }

    @Override
    public GoodsInfo getGoodsInfoById(Long id) {
        GoodsInfo goodsInfo = goodsInfoMapper.selectByPrimaryKey(id);
        if (goodsInfo == null) {
            CMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        return goodsInfo;
    }

    @Override
    public Boolean batchUpdateSaleStatus(Long[] ids, int saleStatus, Long createUser) {
        return goodsInfoMapper.batchUpdateSaleStatus(ids, saleStatus, createUser) > 0;
    }

    @Override
    public PageResult searchGoodsInfo(PageQueryUtil pageUtil) {
        List<GoodsInfo> goodsList = goodsInfoMapper.findGoodsListBySearch(pageUtil);
        int total = goodsInfoMapper.getTotalGoodsBySearch(pageUtil);
        List<SearchPageGoodsVO> searchPageGoodsVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            searchPageGoodsVOList = BeanUtil.copyList(goodsList, SearchPageGoodsVO.class);
            for (SearchPageGoodsVO searchPageGoodsVO : searchPageGoodsVOList) {
                String goodsName = searchPageGoodsVO.getGoodsName();
                String goodsIntro = searchPageGoodsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                    searchPageGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 30) {
                    goodsIntro = goodsIntro.substring(0, 30) + "...";
                    searchPageGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        return new PageResult(searchPageGoodsVOList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public List<GoodsNameVO> getAllGoodsIdsAndNames(Long createUser) {
        List<GoodsInfo> goodsList = goodsInfoMapper.selectByCreateUser(createUser);
        return BeanUtil.copyList(goodsList, GoodsNameVO.class);
    }
}
