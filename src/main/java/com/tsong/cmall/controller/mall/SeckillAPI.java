package com.tsong.cmall.controller.mall;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.config.annotation.NoRepeatSubmit;
import com.tsong.cmall.config.annotation.TokenToMallUser;
import com.tsong.cmall.controller.mall.param.SeckillExeParam;
import com.tsong.cmall.controller.vo.SeckillGoodsVO;
import com.tsong.cmall.controller.vo.SeckillSuccessVO;
import com.tsong.cmall.entity.MallUser;
import com.tsong.cmall.entity.Seckill;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.redis.RedisCache;
import com.tsong.cmall.service.SeckillService;
import com.tsong.cmall.util.MD5Util;
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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @Author Tsong
 * @Date 2023/4/6 21:05
 */
@RestController
@Tag(name = "seckill", description = "1-9.秒杀页面接口")
@RequestMapping("/api")
public class SeckillAPI {
    private static final Logger logger = LoggerFactory.getLogger(SeckillAPI.class);
    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedisCache redisCache;

    @GetMapping("/seckill/time/now")
    @Operation(summary = "获取服务器时间", description = "")
    public Result getTimeNow() {
        return ResultGenerator.genSuccessResult(new Date().getTime());
    }

    @GetMapping("/seckill/checkStock/{seckillId}")
    @Operation(summary = "判断秒杀商品的虚拟库存是否足够", description = "")
    public Result seckillCheckStock(@Parameter(name = "秒杀事件id") @PathVariable Long seckillId,
                                    @TokenToMallUser MallUser loginMallUser) {
        logger.info("seckill seckillCheckStock api,userId={}", loginMallUser.getUserId());
        Integer stock = redisCache.getCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock == null || stock <= 0) {
            return ResultGenerator.genFailResult("秒杀商品库存不足");
        }
        // redis虚拟库存大于等于0时，可以执行秒杀
        return ResultGenerator.genSuccessResult();
    }

    @GetMapping("/seckill/exposer/{seckillId}")
    @Operation(summary = "暴露秒杀链接", description = "")
    public Result exposeUrl(@Parameter(name = "秒杀事件id") @PathVariable Long seckillId,
                            @TokenToMallUser MallUser loginMallUser) {
        logger.info("seckill exposeUrl api,userId={}", loginMallUser.getUserId());
        return ResultGenerator.genSuccessResult(seckillService.exposeUrl(seckillId));
    }


    @PostMapping("/seckill/execute")
    @Operation(summary = "处理秒杀", description = "")
    @NoRepeatSubmit
    public Result execute(@Parameter(name = "秒杀事件参数") @RequestBody @Valid SeckillExeParam seckillExeParam,
                          @TokenToMallUser MallUser loginMallUser){
        String md5 = seckillExeParam.getMd5();
        Long seckillId = seckillExeParam.getSeckillId();
        Long mallUserId = loginMallUser.getUserId();
        // 判断md5信息是否合法
        if (!md5.equals(MD5Util.MD5Encode(seckillId.toString(), Constants.UTF_ENCODING))) {
            throw new CMallException("秒杀商品不存在");
        }
        SeckillSuccessVO seckillSuccessVO = seckillService.executeSeckill(seckillId, mallUserId);
        return ResultGenerator.genSuccessResult(seckillSuccessVO);
    }

    @GetMapping("/seckill/list")
    @Operation(summary = "秒杀商品列表", description = "")
    public Result secondKillGoodsList(@TokenToMallUser MallUser loginMallUser) {
        logger.info("seckill secondKillGoodsList api,userId={}", loginMallUser.getUserId());
        // 直接返回配置的秒杀商品列表
        // 不返回商品id，每配置一条秒杀数据，就生成一个唯一的秒杀id和发起秒杀的事件id，根据秒杀id去访问详情页
        List<SeckillGoodsVO> seckillGoodsVOList = redisCache.getCacheObject(Constants.SECKILL_GOODS_LIST);
        if (seckillGoodsVOList == null) {
            seckillGoodsVOList = seckillService.getSeckillGoodsList();
            // 放入redis
            redisCache.setCacheObject(Constants.SECKILL_GOODS_LIST, seckillGoodsVOList, 60 * 60 * 2, TimeUnit.SECONDS);
        }
        return ResultGenerator.genSuccessResult(seckillGoodsVOList);
    }

    @GetMapping("/seckill/{seckillId}")
    @Operation(summary = "秒杀商品信息", description = "")
    public Result seckillGoodsDetail(@Parameter(name = "秒杀事件id") @PathVariable Long seckillId, @TokenToMallUser MallUser loginMallUser){
        logger.info("seckill seckillGoodsDetail api,userId={}", loginMallUser.getUserId());
        // 返回秒杀商品详情VO，如果秒杀时间未到，不允许访问详情页，也不允许返回数据，参数为秒杀id
        // 根据返回的数据解析出秒杀的事件id，发起秒杀
        // 不访问详情页不会获取到秒杀的事件id，不然容易被猜到url路径从而直接发起秒杀请求
        SeckillGoodsVO seckillGoodsVO = redisCache.getCacheObject(Constants.SECKILL_GOODS_DETAIL + seckillId);
        if (seckillGoodsVO == null) {
            // 获得一个秒杀事件
            Seckill seckill = seckillService.getSeckillById(seckillId);
            if (!seckill.getSeckillStatus()) {
                return ResultGenerator.genFailResult("秒杀商品已下架");
            }
            // 转化成view object 方便前端获取数据（后端->前端）
            seckillGoodsVO = seckillService.getSeckillGoodsDetail(seckill);
            // 放入redis
            redisCache.setCacheObject(Constants.SECKILL_GOODS_DETAIL + seckillId, seckillGoodsVO);
        }
        return ResultGenerator.genSuccessResult(seckillGoodsVO);
    }

}
