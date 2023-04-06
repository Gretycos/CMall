package com.tsong.cmall.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.SeckillStatusEnum;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.controller.vo.SeckillGoodsVO;
import com.tsong.cmall.controller.vo.SeckillSuccessVO;
import com.tsong.cmall.controller.vo.UrlExposerVO;
import com.tsong.cmall.dao.GoodsInfoMapper;
import com.tsong.cmall.dao.SeckillMapper;
import com.tsong.cmall.dao.SeckillSuccessMapper;
import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.entity.Seckill;
import com.tsong.cmall.entity.SeckillSuccess;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.redis.RedisCache;
import com.tsong.cmall.service.SeckillService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.MD5Util;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author Tsong
 * @Date 2023/3/25 13:42
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    // 使用令牌桶RateLimiter 限流
    private static final RateLimiter rateLimiter = RateLimiter.create(100);

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private SeckillSuccessMapper seckillSuccessMapper;

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public PageResult getSeckillPage(PageQueryUtil pageUtil) {
        List<Seckill> carousels = seckillMapper.findSeckillList(pageUtil);
        int total = seckillMapper.getTotalSeckills(pageUtil);
        return new PageResult(carousels, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public boolean saveSeckill(Seckill seckill) {
        if (goodsInfoMapper.selectByPrimaryKey(seckill.getGoodsId()) == null) {
            CMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        return seckillMapper.insertSelective(seckill) > 0;
    }

    @Override
    public boolean updateSeckill(Seckill seckill) {
        if (goodsInfoMapper.selectByPrimaryKey(seckill.getGoodsId()) == null) {
            CMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        Seckill temp = seckillMapper.selectByPrimaryKey(seckill.getSeckillId());
        if (temp == null) {
            CMallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        // 更新时间
        seckill.setUpdateTime(new Date());
        return seckillMapper.updateByPrimaryKeySelective(seckill) > 0;
    }

    @Override
    public Seckill getSeckillById(Long id) {
        return seckillMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean deleteSeckillById(Long id) {
        return seckillMapper.deleteByPrimaryKey(id) > 0;
    }

    @Override
    public List<Seckill> getHomePageSeckill() {
        return seckillMapper.findHomePageSeckillList();
    }

    @Override
    public UrlExposerVO exposeUrl(Long seckillId) {
        SeckillGoodsVO seckillGoodsVO = redisCache.getCacheObject(Constants.SECKILL_GOODS_DETAIL + seckillId);
        Date startTime = seckillGoodsVO.getSeckillBegin();
        Date endTime = seckillGoodsVO.getSeckillEnd();
        // 系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new UrlExposerVO(SeckillStatusEnum.NOT_START, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        // 检查虚拟库存
        Integer stock = redisCache.getCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock == null || stock < 0) {
            return new UrlExposerVO(SeckillStatusEnum.STARTED_SHORTAGE_STOCK, seckillId);
        }
        // 加密
        String md5 = MD5Util.MD5Encode(seckillId.toString(), Constants.UTF_ENCODING);
        return new UrlExposerVO(SeckillStatusEnum.START, md5, seckillId);
    }

    @Override
    public SeckillSuccessVO executeSeckill(Long seckillId, Long userId) {
        // 判断能否在500毫秒内得到令牌，如果不能则立即返回false，不会阻塞程序
        if (!rateLimiter.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            throw new CMallException("秒杀失败");
        }
        // 判断用户是否购买过秒杀商品
        if (redisCache.containsCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userId)) {
            throw new CMallException("您已经购买过秒杀商品，请勿重复购买");
        }
        // 更新秒杀商品虚拟库存
        Long stock = redisCache.luaDecrement(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock < 0) {
            throw new CMallException("秒杀商品已售空");
        }
        // 从redis中获得秒杀
        Seckill seckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        // 如果redis中没有，则从mysql中获取，存入redis中
        if (seckill == null) {
            seckill = seckillMapper.selectByPrimaryKey(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, seckill, 24, TimeUnit.HOURS);
        }
        // 判断秒杀商品是否在有效期内
        long beginTime = seckill.getSeckillBegin().getTime();
        long endTime = seckill.getSeckillEnd().getTime();
        Date now = new Date();
        long nowTime = now.getTime();
        if (nowTime < beginTime) {
            throw new CMallException("秒杀未开启");
        } else if (nowTime > endTime) {
            throw new CMallException("秒杀已结束");
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("seckillId", seckillId);
        map.put("userId", userId);
        map.put("killTime", new Date());
        map.put("result", null);
        // 执行存储过程，result被赋值
        try {
            seckillMapper.killByProcedure(map);
        } catch (Exception e) {
            throw new CMallException(e.getMessage());
        }
        // 获取result -2sql执行失败 -1未插入数据 0未更新数据 1sql执行成功
        // map.get("result");
        int result = MapUtils.getInteger(map, "result", -2);
        if (result != 1) {
            throw new CMallException("很遗憾！未抢购到秒杀商品");
        }
        // result == 1 说明秒杀成功，并且秒杀成功表插入了一条该用户秒杀成功的数据

        // 在redis中记录该用户完成了该秒杀
        redisCache.setCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userId);
        long endExpireTime = endTime / 1000;
        long nowExpireTime = nowTime / 1000;
        redisCache.expire(Constants.SECKILL_SUCCESS_USER_ID + seckillId,
                endExpireTime - nowExpireTime, TimeUnit.SECONDS);

        // 获得该用户的秒杀成功
        SeckillSuccess seckillSuccess = seckillSuccessMapper.getSeckillSuccessByUserIdAndSeckillId(
                userId, seckillId);
        // 传回前端结果
        SeckillSuccessVO seckillSuccessVO = new SeckillSuccessVO();
        Long seckillSuccessId = seckillSuccess.getSecId();
        seckillSuccessVO.setSeckillSuccessId(seckillSuccessId);
        seckillSuccessVO.setMd5(
                MD5Util.MD5Encode(
                        seckillSuccessId + Constants.SECKILL_ORDER_SALT, Constants.UTF_ENCODING));
        return seckillSuccessVO;
    }

    @Override
    public SeckillGoodsVO getSeckillGoodsDetail(Seckill seckill) {
        SeckillGoodsVO seckillGoodsVO = new SeckillGoodsVO();
        BeanUtil.copyProperties(seckill, seckillGoodsVO);

        // 秒杀的商品
        GoodsInfo goodsInfo = goodsInfoMapper.selectByPrimaryKey(seckill.getGoodsId());
        seckillGoodsVO.setGoodsName(goodsInfo.getGoodsName());
        seckillGoodsVO.setGoodsIntro(goodsInfo.getGoodsIntro());
        seckillGoodsVO.setGoodsDetailContent(goodsInfo.getGoodsDetailContent());
        seckillGoodsVO.setGoodsCoverImg(goodsInfo.getGoodsCoverImg());
        seckillGoodsVO.setSellingPrice(goodsInfo.getSellingPrice());
        Date seckillBegin = seckillGoodsVO.getSeckillBegin();
        Date seckillEnd = seckillGoodsVO.getSeckillEnd();
        seckillGoodsVO.setStartDate(seckillBegin.getTime());
        seckillGoodsVO.setEndDate(seckillEnd.getTime());
        return seckillGoodsVO;
    }

    @Override
    public List<SeckillGoodsVO> getSeckillGoodsList() {
        List<Seckill> seckillList = seckillMapper.findHomePageSeckillList();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        return seckillList.stream().map(seckill -> {
            SeckillGoodsVO seckillGoodsVO = new SeckillGoodsVO();
            BeanUtil.copyProperties(seckill, seckillGoodsVO);
            GoodsInfo goodsInfo = goodsInfoMapper.selectByPrimaryKey(seckill.getGoodsId());
            if (goodsInfo == null) {
                return null;
            }
            seckillGoodsVO.setGoodsName(goodsInfo.getGoodsName());
            seckillGoodsVO.setGoodsCoverImg(goodsInfo.getGoodsCoverImg());
            seckillGoodsVO.setSellingPrice(goodsInfo.getSellingPrice());
            Date seckillBegin = seckillGoodsVO.getSeckillBegin();
            Date seckillEnd = seckillGoodsVO.getSeckillEnd();
            String formatBegin = sdf.format(seckillBegin);
            String formatEnd = sdf.format(seckillEnd);
            seckillGoodsVO.setSeckillBeginTime(formatBegin);
            seckillGoodsVO.setSeckillEndTime(formatEnd);
            return seckillGoodsVO;
        }).filter(Objects::nonNull).toList();
    }
}
