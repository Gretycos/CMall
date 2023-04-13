package com.tsong.cmall.controller.admin;

import com.tsong.cmall.common.HomePageConfigTypeEnum;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.TokenToAdminUser;
import com.tsong.cmall.controller.admin.param.BatchIdParam;
import com.tsong.cmall.controller.admin.param.HomePageConfigAddParam;
import com.tsong.cmall.controller.admin.param.HomePageConfigEditParam;
import com.tsong.cmall.entity.AdminUserToken;
import com.tsong.cmall.entity.HomePageConfig;
import com.tsong.cmall.service.HomePageConfigService;
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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Tsong
 * @Date 2023/4/3 16:49
 */
@RestController
@Tag(name = "Admin HomePage Config", description = "2-4.后台管理系统首页配置模块接口")
@RequestMapping("/admin")
public class AdminHomePageConfigAPI {
    private static final Logger logger = LoggerFactory.getLogger(AdminHomePageConfigAPI.class);

    @Autowired
    private HomePageConfigService homePageConfigService;

    /**
     * 列表
     */
    @RequestMapping(value = "/homepageConfigs", method = RequestMethod.GET)
    @Operation(summary = "首页配置列表", description = "首页配置列表")
    public Result list(@RequestParam(required = false) @Parameter(name = "页码") Integer pageNumber,
                       @RequestParam(required = false) @Parameter(name = "每页条数") Integer pageSize,
                       @RequestParam(required = false) @Parameter(name = "1-搜索框热搜 2-搜索下拉框热搜 3-(首页)热销商品 4-(首页)新品上线 5-(首页)为你推荐")
                           Integer configType, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (pageNumber == null || pageNumber < 1 || pageSize == null || pageSize < 10) {
            return ResultGenerator.genFailResult("分页参数异常！");
        }
        HomePageConfigTypeEnum indexConfigTypeEnum = HomePageConfigTypeEnum.getHomePageConfigTypeEnumByType(configType);
        if (indexConfigTypeEnum.equals(HomePageConfigTypeEnum.DEFAULT)) {
            return ResultGenerator.genFailResult("非法参数！");
        }
        Map<String, Object> params = new HashMap<>(8);
        params.put("page", pageNumber);
        params.put("limit", pageSize);
        params.put("configType", configType);
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(homePageConfigService.getConfigsPage(pageUtil));
    }

    /**
     * 添加
     */
    @RequestMapping(value = "/homepageConfigs", method = RequestMethod.POST)
    @Operation(summary = "新增首页配置项", description = "新增首页配置项")
    public Result save(@RequestBody @Valid HomePageConfigAddParam homePageConfigAddParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        HomePageConfig homePageConfig = new HomePageConfig();
        BeanUtil.copyProperties(homePageConfigAddParam, homePageConfig);
        String result = homePageConfigService.saveHomePageConfig(homePageConfig);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }


    /**
     * 修改
     */
    @RequestMapping(value = "/homepageConfigs", method = RequestMethod.PUT)
    @Operation(summary = "修改首页配置项", description = "修改首页配置项")
    public Result update(@RequestBody @Valid HomePageConfigEditParam homePageConfigEditParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        HomePageConfig homePageConfig = new HomePageConfig();
        BeanUtil.copyProperties(homePageConfigEditParam, homePageConfig);
        String result = homePageConfigService.updateHomePageConfig(homePageConfig);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 详情
     */
    @RequestMapping(value = "/homepageConfigs/{id}", method = RequestMethod.GET)
    @Operation(summary = "获取单条首页配置项信息", description = "根据id查询")
    public Result info(@PathVariable("id") Long id, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        HomePageConfig config = homePageConfigService.getHomePageConfigById(id);
        if (config == null) {
            return ResultGenerator.genFailResult("未查询到数据");
        }
        return ResultGenerator.genSuccessResult(config);
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/homepageConfigs", method = RequestMethod.DELETE)
    @Operation(summary = "批量删除首页配置项信息", description = "批量删除首页配置项信息")
    public Result delete(@RequestBody BatchIdParam batchIdParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (batchIdParam == null || batchIdParam.getIds().length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        if (homePageConfigService.deleteBatch(batchIdParam.getIds())) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("删除失败");
        }
    }
}
