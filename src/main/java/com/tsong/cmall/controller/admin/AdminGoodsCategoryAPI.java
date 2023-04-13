package com.tsong.cmall.controller.admin;

import com.tsong.cmall.common.CategoryLevelEnum;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.TokenToAdminUser;
import com.tsong.cmall.controller.admin.param.BatchIdParam;
import com.tsong.cmall.controller.admin.param.GoodsCategoryAddParam;
import com.tsong.cmall.controller.admin.param.GoodsCategoryEditParam;
import com.tsong.cmall.controller.vo.CategoryResultVO;
import com.tsong.cmall.entity.AdminUserToken;
import com.tsong.cmall.entity.GoodsCategory;
import com.tsong.cmall.service.GoodsCategoryService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Tsong
 * @Date 2023/4/3 14:15
 */
@RestController
@Tag(name = "Admin Goods Category", description = "2-2.后台管理系统分类模块接口")
@RequestMapping("/admin")
public class AdminGoodsCategoryAPI {
    private static final Logger logger = LoggerFactory.getLogger(AdminGoodsCategoryAPI.class);
    @Autowired
    private GoodsCategoryService goodsCategoryService;

    /**
     * 列表
     */
    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    @Operation(summary = "商品分类列表", description = "根据级别和上级分类id查询")
    public Result list(@RequestParam(required = false) @Parameter(name = "页码") Integer pageNumber,
                       @RequestParam(required = false) @Parameter(name = "每页条数") Integer pageSize,
                       @RequestParam(required = false) @Parameter(name = "分类级别") Integer categoryLevel,
                       @RequestParam(required = false) @Parameter(name = "上级分类的id") Long parentId,
                       @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (pageNumber == null || pageNumber < 1
                || pageSize == null || pageSize < 10
                || categoryLevel == null || categoryLevel < 0 || categoryLevel > 3
                || parentId == null || parentId < 0) {
            return ResultGenerator.genFailResult("分页参数异常！");
        }
        Map<String, Object> params = new HashMap(8);
        params.put("page", pageNumber);
        params.put("limit", pageSize);
        params.put("categoryLevel", categoryLevel);
        params.put("parentId", parentId);
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(goodsCategoryService.getCategoriesPage(pageUtil));
    }

    /**
     * 列表
     */
    @RequestMapping(value = "/categories4Select", method = RequestMethod.GET)
    @Operation(summary = "商品分类列表", description = "用于三级分类联动效果制作")
    public Result listForSelect(@RequestParam("categoryId") Long categoryId, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (categoryId == null || categoryId < 1) {
            return ResultGenerator.genFailResult("缺少参数！");
        }
        GoodsCategory category = goodsCategoryService.getGoodsCategoryById(categoryId);
        //既不是一级分类也不是二级分类则为不返回数据
        if (category == null || category.getCategoryLevel() == CategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        CategoryResultVO categoryResultVO = new CategoryResultVO();
        if (category.getCategoryLevel() == CategoryLevelEnum.LEVEL_ONE.getLevel()) {
            //如果是一级分类则返回当前一级分类下的所有二级分类，以及二级分类列表中第一条数据下的所有三级分类列表
            //查询一级分类列表中第一个实体的所有二级分类
            List<GoodsCategory> secondLevelCategories = goodsCategoryService.selectByLevelAndParentIdsAndNumber(
                    Collections.singletonList(categoryId), CategoryLevelEnum.LEVEL_TWO.getLevel());
            if (!CollectionUtils.isEmpty(secondLevelCategories)) {
                //查询二级分类列表中第一个实体的所有三级分类
                List<GoodsCategory> thirdLevelCategories = goodsCategoryService.selectByLevelAndParentIdsAndNumber(
                        Collections.singletonList(secondLevelCategories.get(0).getCategoryId()), CategoryLevelEnum.LEVEL_THREE.getLevel());
                categoryResultVO.setSecondLevelCategories(secondLevelCategories);
                categoryResultVO.setThirdLevelCategories(thirdLevelCategories);
            }
        }
        if (category.getCategoryLevel() == CategoryLevelEnum.LEVEL_TWO.getLevel()) {
            //如果是二级分类则返回当前分类下的所有三级分类列表
            List<GoodsCategory> thirdLevelCategories = goodsCategoryService.selectByLevelAndParentIdsAndNumber(
                    Collections.singletonList(categoryId), CategoryLevelEnum.LEVEL_THREE.getLevel());
            categoryResultVO.setThirdLevelCategories(thirdLevelCategories);
        }
        return ResultGenerator.genSuccessResult(categoryResultVO);
    }

    /**
     * 添加
     */
    @RequestMapping(value = "/categories", method = RequestMethod.POST)
    @Operation(summary = "新增分类", description = "新增分类")
    public Result save(@RequestBody @Valid GoodsCategoryAddParam goodsCategoryAddParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        GoodsCategory goodsCategory = new GoodsCategory();
        BeanUtil.copyProperties(goodsCategoryAddParam, goodsCategory);
        String result = goodsCategoryService.saveCategory(goodsCategory);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }


    /**
     * 修改
     */
    @RequestMapping(value = "/categories", method = RequestMethod.PUT)
    @Operation(summary = "修改分类信息", description = "修改分类信息")
    public Result update(@RequestBody @Valid GoodsCategoryEditParam goodsCategoryEditParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        GoodsCategory goodsCategory = new GoodsCategory();
        BeanUtil.copyProperties(goodsCategoryEditParam, goodsCategory);
        String result = goodsCategoryService.updateGoodsCategory(goodsCategory);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 详情
     */
    @RequestMapping(value = "/categories/{id}", method = RequestMethod.GET)
    @Operation(summary = "获取单条分类信息", description = "根据id查询")
    public Result info(@PathVariable("id") Long id, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        GoodsCategory goodsCategory = goodsCategoryService.getGoodsCategoryById(id);
        if (goodsCategory == null) {
            return ResultGenerator.genFailResult("未查询到数据");
        }
        return ResultGenerator.genSuccessResult(goodsCategory);
    }

    /**
     * 分类删除
     */
    @RequestMapping(value = "/categories", method = RequestMethod.DELETE)
    @Operation(summary = "批量删除分类信息", description = "批量删除分类信息")
    public Result delete(@RequestBody BatchIdParam batchIdParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (batchIdParam == null || batchIdParam.getIds().length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        if (goodsCategoryService.deleteBatch(batchIdParam.getIds())) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("删除失败");
        }
    }
}
