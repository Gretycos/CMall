package com.tsong.cmall.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.SeckillStatusEnum;
import com.tsong.cmall.common.ServiceResultEnum;
import com.tsong.cmall.config.annotation.Master;
import com.tsong.cmall.config.annotation.Slave;
import com.tsong.cmall.controller.vo.*;
import com.tsong.cmall.dao.GoodsInfoMapper;
import com.tsong.cmall.dao.SeckillMapper;
import com.tsong.cmall.dao.SeckillSuccessMapper;
import com.tsong.cmall.entity.GoodsInfo;
import com.tsong.cmall.entity.Seckill;
import com.tsong.cmall.entity.SeckillSuccess;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.redis.RedisCache;
import com.tsong.cmall.service.SeckillService;
import com.tsong.cmall.task.SeckillOrderUnsubmitTask;
import com.tsong.cmall.task.TaskService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.MD5Util;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tsong.cmall.common.ServiceResultEnum.DATA_NOT_EXIST;

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

    @Autowired
    private TaskService taskService;

    @Override
    public PageResult getSeckillPage(PageQueryUtil pageUtil) {
        List<Seckill> seckillList = seckillMapper.findSeckillList(pageUtil);
        int total = seckillMapper.getTotalSeckills(pageUtil);
        // 更新过期
        List<SeckillVO> expiredSeckillList = new ArrayList<>();
        // 返回结果
        List<SeckillVO> seckillVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(seckillList)){
            Date now = new Date();
            // 映射商品id列表
            List<Long> goodsIdList = seckillList.stream().map(Seckill::getGoodsId).toList();
            // 查询商品列表
            List<GoodsInfo> goodsInfoList = goodsInfoMapper.selectByPrimaryKeys(goodsIdList);
            // 映射成map {goodsId: GoodsInfo}
            Map<Long, GoodsInfo> goodsInfoMap = goodsInfoList.stream().collect(
                    Collectors.toMap(GoodsInfo::getGoodsId, Function.identity(), (e1, e2) -> e1));

            seckillVOList = BeanUtil.copyList(seckillList, SeckillVO.class);
            for (SeckillVO seckillVO : seckillVOList) {
                if (seckillVO.getSeckillEnd().getTime() < now.getTime()){
                    if (seckillVO.getSeckillStatus()){
                        seckillVO.setSeckillStatus(false);
                        expiredSeckillList.add(seckillVO);
                    }
                }
                GoodsInfo goodsInfo = goodsInfoMap.get(seckillVO.getGoodsId());
                if (goodsInfo == null){
                    CMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
                }
                seckillVO.setGoodsName(goodsInfo.getGoodsName());
                seckillVO.setGoodsCoverImg(goodsInfo.getGoodsCoverImg());
            }
            if (!expiredSeckillList.isEmpty()){
                List<Long> seckillIds = expiredSeckillList.stream().map(SeckillVO::getSeckillId).toList();
                if (seckillMapper.putOffBatch(seckillIds) <= 0){
                    CMallException.fail("无法设置秒杀过期下架");
                }
                deleteSeckillFromCache(seckillIds);
            }
        }
        return new PageResult(seckillVOList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public SeckillVO getSeckillVOById(Long id) {
        Seckill seckill = getSeckillById(id);
        SeckillVO seckillVO = new SeckillVO();
        BeanUtil.copyProperties(seckill, seckillVO);
        GoodsInfo goodsInfo = goodsInfoMapper.selectByPrimaryKey(seckill.getGoodsId());
        if (goodsInfo == null){
            CMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        seckillVO.setGoodsName(goodsInfo.getGoodsName());
        seckillVO.setGoodsCoverImg(goodsInfo.getGoodsCoverImg());
        return seckillVO;
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
        Seckill temp = seckillMapper.selectByPrimaryKey(seckill.getSeckillId());
        if (temp == null) {
            CMallException.fail(DATA_NOT_EXIST.getResult());
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
    @Slave
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
        if (stock == null || stock <= 0) {
            return new UrlExposerVO(SeckillStatusEnum.STARTED_SHORTAGE_STOCK, seckillId);
        }
        // 加密
        String md5 = MD5Util.MD5Encode(seckillId.toString(), Constants.UTF_ENCODING);
        return new UrlExposerVO(SeckillStatusEnum.START, md5, seckillId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Master
    public SeckillSuccessVO executeSeckill(Long seckillId, Long userId) {
        // 判断能否在500毫秒内得到令牌，如果不能则立即返回false，不会阻塞程序
        if (!rateLimiter.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            CMallException.fail("秒杀失败");
        }
        // 判断用户是否购买过秒杀商品
        if (redisCache.containsCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userId)) {
            CMallException.fail("您已经购买过秒杀商品，请勿重复购买");
        }
        // 更新秒杀商品虚拟库存
        // 剩余库存
        Long stock = redisCache.luaDecrement(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock < 0) {
            CMallException.fail("秒杀商品已售空");
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
        long nowTime = System.currentTimeMillis();

        // 在redis中记录该用户完成了该秒杀
        redisCache.setCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userId);
        redisCache.expire(Constants.SECKILL_SUCCESS_USER_ID + seckillId, endTime - nowTime, TimeUnit.MILLISECONDS);

        if (nowTime < beginTime) {
            CMallException.fail("秒杀未开启");
        } else if (nowTime > endTime) {
            CMallException.fail("秒杀已结束");
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
            e.printStackTrace();
            CMallException.fail("服务器异常");
        }
        // 获取result -2sql执行失败 -1未插入数据 0未更新数据 1sql执行成功
        // map.get("result");
        int result = MapUtils.getInteger(map, "result", -2);
        if (result != 1) {
            CMallException.fail("很遗憾！未抢购到秒杀商品");
        }
        // result == 1 说明秒杀成功，并且秒杀成功表插入了一条该用户秒杀成功的数据

        // 获得该用户的秒杀成功
        SeckillSuccess seckillSuccess = null;
        try{
            seckillSuccess = seckillSuccessMapper.getSeckillSuccessByUserIdAndSeckillId(
                    userId, seckillId);
        }catch (Exception e){
            e.printStackTrace();
            // 恢复虚拟缓存库存
            redisCache.increment(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
            CMallException.fail("您已经成功购买，请前往付款");
        }
        // 传回前端结果
        SeckillSuccessVO seckillSuccessVO = new SeckillSuccessVO();
        Long seckillSuccessId = seckillSuccess.getSecId();
        seckillSuccessVO.setSeckillSuccessId(seckillSuccessId);
        seckillSuccessVO.setMd5(
                MD5Util.MD5Encode(
                        seckillSuccessId + Constants.SECKILL_ORDER_SALT, Constants.UTF_ENCODING));
        // 秒杀成功后未提交订单
        taskService.addTask(new SeckillOrderUnsubmitTask(seckillSuccessId,60 * 1 * 1000));
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
        seckillGoodsVO.setGoodsCarousel(goodsInfo.getGoodsCarousel().split(","));
        seckillGoodsVO.setSellingPrice(goodsInfo.getSellingPrice());
        seckillGoodsVO.setSeckillBegin(seckillGoodsVO.getSeckillBegin());
        seckillGoodsVO.setSeckillEnd(seckillGoodsVO.getSeckillEnd());
        return seckillGoodsVO;
    }

    @Override
    public List<SeckillGoodsVO> getSeckillGoodsList() {
        List<Seckill> seckillList = seckillMapper.findHomePageSeckillList();
        Date now = new Date();
        List<Seckill> expiredSeckillList = new ArrayList<>();
        List<SeckillGoodsVO> res = seckillList.stream().map(seckill -> {
            SeckillGoodsVO seckillGoodsVO = new SeckillGoodsVO();
            BeanUtil.copyProperties(seckill, seckillGoodsVO);
            if (seckill.getSeckillEnd().getTime() < now.getTime()){
                expiredSeckillList.add(seckill);
                return null;
            }
            GoodsInfo goodsInfo = goodsInfoMapper.selectByPrimaryKey(seckill.getGoodsId());
            if (goodsInfo == null) {
                return null;
            }
            seckillGoodsVO.setGoodsName(goodsInfo.getGoodsName());
            seckillGoodsVO.setGoodsCoverImg(goodsInfo.getGoodsCoverImg());
            seckillGoodsVO.setSellingPrice(goodsInfo.getSellingPrice());
            return seckillGoodsVO;
        }).filter(Objects::nonNull).toList();
        // 每次查询时会自动过滤已经过期的秒杀
        // 并且把过期的秒杀设置为下架状态
        if (!expiredSeckillList.isEmpty()){
            List<Long> seckillIds = expiredSeckillList.stream().map(Seckill::getSeckillId).toList();
            if (seckillMapper.putOffBatch(seckillIds) <= 0){
                CMallException.fail("无法设置秒杀过期下架");
            }
            deleteSeckillFromCache(seckillIds);
        }
        return res;
    }

    private void deleteSeckillFromCache(List<Long> seckillIds){
        for (Long seckillId : seckillIds) {
            redisCache.deleteObject(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
            redisCache.deleteObject(Constants.SECKILL_GOODS_DETAIL + seckillId);
        }
        redisCache.deleteObject(Constants.SECKILL_GOODS_LIST);
    }
}
