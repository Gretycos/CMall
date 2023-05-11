package com.tsong.cmall.controller.admin;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.config.annotation.TokenToAdminUser;
import com.tsong.cmall.controller.admin.param.SeckillAddParam;
import com.tsong.cmall.controller.admin.param.SeckillEditParam;
import com.tsong.cmall.controller.vo.SeckillVO;
import com.tsong.cmall.entity.AdminUserToken;
import com.tsong.cmall.entity.Seckill;
import com.tsong.cmall.redis.RedisCache;
import com.tsong.cmall.service.SeckillService;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Tsong
 * @Date 2023/4/12 13:44
 */
@RestController
@Tag(name = "Admin Seckill", description = "2-9.后台管理秒杀模块接口")
@RequestMapping("/admin")
public class AdminSeckillAPI {
    private static final Logger logger = LoggerFactory.getLogger(AdminSeckillAPI.class);
    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedisCache redisCache;

    @GetMapping("/seckill")
    @Operation(summary = "秒杀商品列表", description = "")
    public Result seckillList(@Parameter(name = "页码") @RequestParam(required = false) Integer pageNumber,
                             @Parameter(name = "每页条数") @RequestParam(required = false) Integer pageSize,
                             @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("seckillList, adminUser:{}", adminUser.toString());
        Map<String, Object> params = new HashMap<>(8);
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize == null || pageSize < 10 || pageSize > 100){
            pageSize = 10;
        }
        params.put("page", pageNumber);
        params.put("limit", pageSize);
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(seckillService.getSeckillPage(pageUtil));
    }

    @PostMapping("/seckill")
    @Operation(summary = "新增秒杀", description = "")
    public Result saveSeckill(@Parameter(name = "秒杀新增参数") @RequestBody @Valid SeckillAddParam seckillAddParam,
                             @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("saveSeckill, adminUser:{}", adminUser.toString());
        Seckill seckill = new Seckill();
        BeanUtil.copyProperties(seckillAddParam, seckill);
        seckill.setCreateTime(new Date());
        boolean result = seckillService.saveSeckill(seckill);
        if (!result){
            return ResultGenerator.genFailResult("新增秒杀失败");
        }
        // 虚拟库存预热
        // mapper文件中使用了useGeneratedKeys="true" keyProperty="seckillId"
        // 所以插入成功后会返回seckillId到对象上
        redisCache.setCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckill.getSeckillId(), seckill.getSeckillNum());
        return ResultGenerator.genSuccessResult();
    }

    @PutMapping("/seckill")
    @Operation(summary = "修改秒杀", description = "")
    public Result updateSeckill(@Parameter(name = "秒杀修改参数") @RequestBody @Valid SeckillEditParam seckillEditParam,
                               @TokenToAdminUser AdminUserToken adminUser){
        logger.info("updateSeckill, adminUser:{}", adminUser.toString());
        Seckill seckill = new Seckill();
        BeanUtil.copyProperties(seckillEditParam, seckill);
        seckill.setUpdateTime(new Date());
        boolean result = seckillService.updateSeckill(seckill);
        if (!result){
            return ResultGenerator.genFailResult("更新秒杀失败");
        }
        // 虚拟库存预热
        redisCache.setCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckill.getSeckillId(), seckill.getSeckillNum());
        redisCache.deleteObject(Constants.SECKILL_GOODS_DETAIL + seckill.getSeckillId());
        redisCache.deleteObject(Constants.SECKILL_GOODS_LIST);
        return ResultGenerator.genSuccessResult();
    }

    @GetMapping("/seckill/{id}")
    @Operation(summary = "秒杀详情", description = "")
    public Result seckillInfo(@Parameter(name = "秒杀id") @PathVariable("id") Long id,
                             @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("seckillInfo, adminUser:{}", adminUser.toString());
        SeckillVO seckillVO = seckillService.getSeckillVOById(id);
        return ResultGenerator.genSuccessResult(seckillVO);
    }

    @DeleteMapping("/seckill/{id}")
    @Operation(summary = "删除秒杀", description = "")
    public Result deleteSeckill(@Parameter(name = "秒杀id") @PathVariable Long id,
                         @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("deleteSeckill, adminUser:{}", adminUser.toString());
        boolean result = seckillService.deleteSeckillById(id);
        if (!result){
            return ResultGenerator.genFailResult("删除秒杀失败");
        }
        // 从缓存中去除
        redisCache.deleteObject(Constants.SECKILL_GOODS_STOCK_KEY + id);
        redisCache.deleteObject(Constants.SECKILL_GOODS_DETAIL + id);
        redisCache.deleteObject(Constants.SECKILL_GOODS_LIST);
        return ResultGenerator.genSuccessResult();
    }

}
