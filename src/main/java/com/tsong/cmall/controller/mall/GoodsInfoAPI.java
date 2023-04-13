package com.tsong.cmall.controller.mall;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.TokenToMallUser;
import com.tsong.cmall.controller.vo.GoodsDetailVO;
import com.tsong.cmall.controller.vo.SearchPageGoodsVO;
import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.GoodsInfoService;
import com.tsong.cmall.util.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Tsong
 * @Date 2023/3/31 12:23
 */
@RestController
@Tag(name = "goods", description = "1-4.CMall商品相关接口")
@RequestMapping("/api")
public class GoodsInfoAPI {
    private static final Logger logger = LoggerFactory.getLogger(GoodsInfoAPI.class);

    @Autowired
    private GoodsInfoService goodsInfoService;

    @GetMapping("/search")
    @Operation(summary = "商品搜索接口", description = "根据关键字和分类id进行搜索")
    public Result<PageResult<List<SearchPageGoodsVO>>> search(@RequestParam(required = false) @Parameter(name = "搜索关键字") String keyword,
                                                              @RequestParam(required = false) @Parameter(name = "分类id") Long goodsCategoryId,
                                                              @RequestParam(required = false) @Parameter(name = "orderBy") String orderBy,
                                                              @RequestParam(required = false) @Parameter(name = "页码") Integer pageNumber,
                                                              @TokenToMallUser MallUser loginMallUser) {

        logger.info("goods search api,keyword={},goodsCategoryId={},orderBy={},pageNumber={},userId={}",
                keyword, goodsCategoryId, orderBy, pageNumber, loginMallUser.getUserId());

        Map<String, Object> params = new HashMap<>(8);
        //两个搜索参数都为空，直接返回异常
        if (goodsCategoryId == null && !StringUtils.hasText(keyword)) {
            CMallException.fail("非法的搜索参数");
        }
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        params.put("goodsCategoryId", goodsCategoryId);
        params.put("page", pageNumber);
        params.put("limit", Constants.GOODS_SEARCH_PAGE_LIMIT);
        //对keyword做过滤 去掉空格
        if (StringUtils.hasText(keyword)) {
            params.put("keyword", keyword);
        }
        if (StringUtils.hasText(orderBy)) {
            params.put("orderBy", orderBy);
        }
        //搜索上架状态下的商品
        params.put("goodsSellStatus", Constants.SELL_STATUS_UP);
        //封装商品数据
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(goodsInfoService.searchGoodsInfo(pageUtil));
    }

    @GetMapping("/goods/detail/{goodsId}")
    @Operation(summary = "商品详情接口", description = "传参为商品id")
    public Result<GoodsDetailVO> goodsDetail(@Parameter(name = "商品id") @PathVariable("goodsId") Long goodsId,
                                             @TokenToMallUser MallUser loginMallUser) {
        logger.info("goods detail api,goodsId={},userId={}", goodsId, loginMallUser.getUserId());
        if (goodsId < 1) {
            return ResultGenerator.genFailResult("参数异常");
        }
        GoodsInfo goods = goodsInfoService.getGoodsInfoById(goodsId);
        if (Constants.SELL_STATUS_UP != goods.getGoodsSaleStatus()) {
            CMallException.fail(ServiceResultEnum.GOODS_PUT_DOWN.getResult());
        }
        GoodsDetailVO goodsDetailVO = new GoodsDetailVO();
        BeanUtil.copyProperties(goods, goodsDetailVO);
        goodsDetailVO.setGoodsCarouselList(goods.getGoodsCarousel().split(","));
        return ResultGenerator.genSuccessResult(goodsDetailVO);
    }
}
