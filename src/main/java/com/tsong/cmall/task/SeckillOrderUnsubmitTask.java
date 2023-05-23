package com.tsong.cmall.task;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.dao.SeckillMapper;
import com.tsong.cmall.dao.SeckillSuccessMapper;
import com.tsong.cmall.entity.SeckillSuccess;
import com.tsong.cmall.redis.RedisCache;
import com.tsong.cmall.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Tsong
 * @Date 2023/5/23 20:23
 */
public class SeckillOrderUnsubmitTask extends Task{

    /**
     * 默认延迟时间30分钟，单位毫秒
     */
    private static final long DELAY_TIME = 60 * 30 * 1000;

    private final Logger log = LoggerFactory.getLogger(SeckillOrderUnsubmitTask.class);

    private final Long seckillSuccessId;

    public SeckillOrderUnsubmitTask(Long seckillSuccessId, long delayInMilliseconds){
        super("SeckillOrderUnsubmitTask-"+seckillSuccessId, delayInMilliseconds);
        this.seckillSuccessId = seckillSuccessId;
    }

    public SeckillOrderUnsubmitTask(Long seckillSuccessId){
        super("SeckillOrderUnsubmitTask-"+seckillSuccessId, DELAY_TIME);
        this.seckillSuccessId = seckillSuccessId;
    }

    @Override
    public void run() {
        log.info("系统开始处理延时任务---秒杀订单超时未提交--- {}", seckillSuccessId);

        SeckillSuccessMapper seckillSuccessMapper = SpringContextUtil.getBean(SeckillSuccessMapper.class);
        SeckillMapper seckillMapper = SpringContextUtil.getBean(SeckillMapper.class);
        RedisCache redisCache = SpringContextUtil.getBean(RedisCache.class);

        SeckillSuccess seckillSuccess = seckillSuccessMapper.selectByPrimaryKey(seckillSuccessId);
        Long seckillId = seckillSuccess.getSeckillId();
        Long userId = seckillSuccess.getUserId();
        // 恢复虚拟缓存库存
        redisCache.increment(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        // 清除缓存中的用户秒杀记录
        redisCache.deleteCacheSetMember(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userId);
        // 恢复数据库秒杀库存
        if (!seckillMapper.addStock(seckillId)){
            throw new RuntimeException("秒杀商品货品库存增加失败");
        }
        // 清除数据库中秒杀成功记录
        if (seckillSuccessMapper.deleteByPrimaryKey(seckillSuccessId) <= 0){
            throw new RuntimeException("秒杀记录清除失败");
        }

        log.info("系统结束处理延时任务---秒杀订单超时未提交--- {}", seckillSuccessId);
    }
}
