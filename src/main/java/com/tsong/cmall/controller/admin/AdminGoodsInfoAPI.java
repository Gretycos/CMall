package com.tsong.cmall.controller.admin;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.TokenToAdminUser;
import com.tsong.cmall.controller.admin.param.BatchIdParam;
import com.tsong.cmall.controller.admin.param.GoodsAddParam;
import com.tsong.cmall.controller.admin.param.GoodsEditParam;
import com.tsong.cmall.controller.vo.GoodsAndCategoryVO;
import com.tsong.cmall.entity.AdminUserToken;
import com.tsong.cmall.entity.GoodsCategory;
import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.service.GoodsCategoryService;
import com.tsong.cmall.service.GoodsInfoService;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Tsong
 * @Date 2023/4/3 16:08
 */
@RestController
@Tag(name = "Goods Info", description = "2-3.后台管理系统商品模块接口")
@RequestMapping("/admin")
public class AdminGoodsInfoAPI {
    private static final Logger logger = LoggerFactory.getLogger(AdminGoodsInfoAPI.class);
    @Autowired
    private GoodsInfoService goodsInfoService;
    @Autowired
    private GoodsCategoryService goodsCategoryService;

    /**
     * 列表
     */
    @RequestMapping(value = "/goods/list", method = RequestMethod.GET)
    @Operation(summary = "商品列表", description = "可根据名称和上架状态筛选")
    public Result list(@RequestParam(required = false) @Parameter(name = "页码") Integer pageNumber,
                       @RequestParam(required = false) @Parameter(name = "每页条数") Integer pageSize,
                       @RequestParam(required = false) @Parameter(name = "商品名称") String goodsName,
                       @RequestParam(required = false) @Parameter(name = "上架状态 0-上架 1-下架") Integer goodsSaleStatus,
                       @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize == null || pageSize < 10 || pageSize > 100){
            pageSize = 10;
        }
        Map<String, Object> params = new HashMap<>(8);
        params.put("page", pageNumber);
        params.put("limit", pageSize);
        if (StringUtils.hasText(goodsName)) {
            params.put("goodsName", goodsName);
        }
        if (goodsSaleStatus != null) {
            params.put("goodsSaleStatus", goodsSaleStatus);
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(goodsInfoService.getGoodsInfoPage(pageUtil));
    }

    /**
     * 添加
     */
    @RequestMapping(value = "/goods", method = RequestMethod.POST)
    @Operation(summary = "新增商品信息", description = "新增商品信息")
    public Result save(@RequestBody @Valid GoodsAddParam goodsAddParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        GoodsInfo goodsInfo = new GoodsInfo();
        BeanUtil.copyProperties(goodsAddParam, goodsInfo);
        String result = goodsInfoService.saveGoodsInfo(goodsInfo);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }


    /**
     * 修改
     */
    @RequestMapping(value = "/goods", method = RequestMethod.PUT)
    @Operation(summary = "修改商品信息", description = "修改商品信息")
    public Result update(@RequestBody @Valid GoodsEditParam goodsEditParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        GoodsInfo goodsInfo = new GoodsInfo();
        BeanUtil.copyProperties(goodsEditParam, goodsInfo);
        String result = goodsInfoService.updateGoodsInfo(goodsInfo);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 详情
     */
    @GetMapping("/goods/{id}")
    @Operation(summary = "获取单条商品信息", description = "根据id查询")
    public Result info(@PathVariable("id") Long id, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        GoodsAndCategoryVO goodsAndCategoryVO = new GoodsAndCategoryVO();
        GoodsInfo goodsInfo = goodsInfoService.getGoodsInfoById(id);
        if (goodsInfo == null) {
            return ResultGenerator.genFailResult(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        goodsAndCategoryVO.setGoodsInfo(goodsInfo);
        GoodsCategory thirdCategory;
        GoodsCategory secondCategory;
        GoodsCategory firstCategory;
        thirdCategory = goodsCategoryService.getGoodsCategoryById(goodsInfo.getGoodsCategoryId());
        if (thirdCategory != null) {
            goodsAndCategoryVO.setThirdCategory(thirdCategory);
            secondCategory = goodsCategoryService.getGoodsCategoryById(thirdCategory.getParentId());
            if (secondCategory != null) {
                goodsAndCategoryVO.setSecondCategory(secondCategory);
                firstCategory = goodsCategoryService.getGoodsCategoryById(secondCategory.getParentId());
                if (firstCategory != null) {
                    goodsAndCategoryVO.setFirstCategory(firstCategory);
                }
            }
        }
        return ResultGenerator.genSuccessResult(goodsAndCategoryVO);
    }

    /**
     * 批量修改销售状态
     */
    @PutMapping(value = "/goods/status/{saleStatus}")
    @Operation(summary = "批量修改销售状态", description = "批量修改销售状态")
    public Result editSaleStatus(@RequestBody BatchIdParam batchIdParam,
                                 @PathVariable("saleStatus") int saleStatus,
                                 @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (batchIdParam == null || batchIdParam.getIds().length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        if (saleStatus != Constants.SELL_STATUS_UP && saleStatus != Constants.SELL_STATUS_DOWN) {
            return ResultGenerator.genFailResult("状态异常！");
        }
        if (goodsInfoService.batchUpdateSaleStatus(batchIdParam.getIds(), saleStatus)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("修改失败");
        }
    }
}
