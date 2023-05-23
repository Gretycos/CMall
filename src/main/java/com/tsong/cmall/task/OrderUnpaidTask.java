package com.tsong.cmall.task;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.common.OrderStatusEnum;
import com.tsong.cmall.dao.*;
import com.tsong.cmall.entity.Order;
import com.tsong.cmall.entity.OrderItem;
import com.tsong.cmall.entity.SeckillSuccess;
import com.tsong.cmall.redis.RedisCache;
import com.tsong.cmall.service.CouponService;
import com.tsong.cmall.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/3/24 18:37
 */
public class OrderUnpaidTask extends Task{

    /**
     * 默认延迟时间30分钟，单位毫秒
     */
    private static final long DELAY_TIME = 60 * 30 * 1000;

    private final Logger log = LoggerFactory.getLogger(OrderUnpaidTask.class);
    /**
     * 订单id
     */
    private final Long orderId;

    public OrderUnpaidTask(Long orderId, long delayInMilliseconds) {
        super("OrderUnpaidTask-" + orderId, delayInMilliseconds);
        this.orderId = orderId;
        log.info("新增订单未支付任务：" + orderId);
    }

    public OrderUnpaidTask(Long orderId) {
        super("OrderUnpaidTask-" + orderId, DELAY_TIME);
        this.orderId = orderId;
    }
    @Override
    public void run() {
        log.info("系统开始处理延时任务---订单超时未付款--- {}", this.orderId);

        // 线程不能直接获取bean，只能通过工具去获取
        OrderMapper orderMapper = SpringContextUtil.getBean(OrderMapper.class);
        OrderItemMapper orderItemMapper = SpringContextUtil.getBean(OrderItemMapper.class);
        GoodsInfoMapper goodsInfoMapper = SpringContextUtil.getBean(GoodsInfoMapper.class);
        SeckillSuccessMapper seckillSuccessMapper = SpringContextUtil.getBean(SeckillSuccessMapper.class);
        CouponService couponService = SpringContextUtil.getBean(CouponService.class);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null || order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            if (order.getOrderStatus() > 0){
                log.info("系统结束处理延时任务---订单已付款--- {}", this.orderId);
            } else {
                log.info("系统结束处理延时任务---订单已关闭--- {}", this.orderId);
            }
            return;
        }

        // 设置订单为已取消状态
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus());
        order.setUpdateTime(new Date());
        if (orderMapper.updateByPrimaryKey(order) <= 0) {
            throw new RuntimeException("更新数据已失效");
        }

        // 用户id
        Long userId = order.getUserId();
        // 商品货品数量增加
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem orderItem : orderItemList) {
            // 如果是秒杀商品
            if (orderItem.getSeckillId() != null) {
                Long seckillId = orderItem.getSeckillId();
                SeckillMapper seckillMapper = SpringContextUtil.getBean(SeckillMapper.class);
                if (!seckillMapper.addStock(seckillId)) {
                    throw new RuntimeException("秒杀商品货品库存增加失败");
                }
                SeckillSuccess seckillSuccess = seckillSuccessMapper.getSeckillSuccessByUserIdAndSeckillId(userId, seckillId);
                if (seckillSuccessMapper.deleteByPrimaryKey(seckillSuccess.getSecId()) <= 0){
                    throw new RuntimeException("秒杀商品货品用户记录清除失败");
                }
                // 获得缓存
                RedisCache redisCache = SpringContextUtil.getBean(RedisCache.class);
                // 自动取消订单的商品数恢复
                redisCache.increment(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
                redisCache.deleteCacheSetMember(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userId);
            } else {
                Long goodsId = orderItem.getGoodsId();
                Integer goodsCount = orderItem.getGoodsCount();
                if (!goodsInfoMapper.addStock(goodsId, goodsCount)) {
                    throw new RuntimeException("商品货品库存增加失败");
                }
            }
        }

        // 返还优惠券
        couponService.releaseCoupon(orderId);
        log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
    }
}
