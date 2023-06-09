package com.tsong.cmall.dao;

import com.tsong.cmall.entity.GoodsCategory;
import com.tsong.cmall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: Tsong
 * @date: 2023/03/20/09:31
 */
public interface GoodsCategoryMapper {
    int deleteByPrimaryKey(Long categoryId);

    int insert(GoodsCategory row);

    int insertSelective(GoodsCategory row);

    GoodsCategory selectByPrimaryKey(Long categoryId);

    int updateByPrimaryKeySelective(GoodsCategory row);

    int updateByPrimaryKey(GoodsCategory row);

    GoodsCategory selectByLevelAndName(Byte categoryLevel, String categoryName);

    List<GoodsCategory> findGoodsCategoryList(PageQueryUtil pageUtil);

    int getTotalGoodsCategories(PageQueryUtil pageUtil);

    int deleteBatch(Long[] ids);

    List<GoodsCategory> selectByLevelAndParentIdsAndNumber(List<Long> parentIds, int categoryLevel, int number);

    List<GoodsCategory> selectByPrimaryKeys(List<Long> ids);

    List<GoodsCategory> selectByLevel(int categoryLevel);
}