package com.tsong.cmall.service;

import com.tsong.cmall.controller.vo.HomePageCategoryVO;
import com.tsong.cmall.controller.vo.SearchPageCategoryVO;
import com.tsong.cmall.entity.GoodsCategory;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;

import java.util.List;

public interface GoodsCategoryService {
    /**
     * @Description 分页
     * @Param [pageUtil]
     * @Return com.tsong.cmall.util.PageResult
     */

    PageResult getCategoriesPage(PageQueryUtil pageUtil);

    String saveCategory(GoodsCategory goodsCategory);

    String updateGoodsCategory(GoodsCategory goodsCategory);

    GoodsCategory getGoodsCategoryById(Long id);

    Boolean deleteBatch(Integer[] ids);

    /**
     * @Description 首页分类数据
     * @Param []
     * @Return java.util.List<com.tsong.cmall.controller.vo.HomePageCategoryVO>
     */

    List<HomePageCategoryVO> getCategoriesForIndex();

    /**
     * @Description 搜索页分类数据
     * @Param [categoryId]
     * @Return com.tsong.cmall.controller.vo.SearchPageCategoryVO
     */

    SearchPageCategoryVO getCategoriesForSearch(Long categoryId);

    /**
     * @Description 根据parentId和level获取分类列表
     * @Param [parentIds, categoryLevel]
     * @Return java.util.List<com.tsong.cmall.entity.GoodsCategory>
     */

    List<GoodsCategory> selectByLevelAndParentIdsAndNumber(List<Long> parentIds, int categoryLevel);
}
