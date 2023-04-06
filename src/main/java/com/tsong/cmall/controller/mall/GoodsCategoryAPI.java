package com.tsong.cmall.controller.mall;

import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.controller.vo.HomePageCategoryVO;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.GoodsCategoryService;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/3/31 22:52
 */
@RestController
@Api(value = "goods category", tags = "1-3.分类页面接口")
@RequestMapping("/api")
public class GoodsCategoryAPI {
    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @GetMapping("/categories")
    @ApiOperation(value = "获取分类数据", notes = "分类页面使用")
    public Result<List<HomePageCategoryVO>> getCategories() {
        List<HomePageCategoryVO> categories = goodsCategoryService.getCategoriesForHomePage();
        if (CollectionUtils.isEmpty(categories)) {
            CMallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        return ResultGenerator.genSuccessResult(categories);
    }
}
