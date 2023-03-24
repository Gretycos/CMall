package com.tsong.cmall.service.impl;

import com.tsong.cmall.common.*;
import com.tsong.cmall.config.ProjectConfig;
import com.tsong.cmall.controller.vo.*;
import com.tsong.cmall.dao.*;
import com.tsong.cmall.entity.*;
import com.tsong.cmall.exception.CMallException;
import com.tsong.cmall.service.OrderService;
import com.tsong.cmall.task.OrderUnpaidTask;
import com.tsong.cmall.task.TaskService;
import com.tsong.cmall.util.BeanUtil;
import com.tsong.cmall.util.NumberUtil;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * @Author Tsong
 * @Date 2023/3/24 15:44
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ShoppingCartItemMapper shoppingCartItemMapper;

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Autowired
    private UserCouponRecordMapper userCouponRecordMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private SeckillSuccessMapper seckillSuccessMapper;

    @Autowired
    private TaskService taskService;

    @Override
    public PageResult<Order> getOrdersPage(PageQueryUtil pageUtil) {
        int total = orderMapper.getTotalOrders(pageUtil);
        List<Order> orderList = orderMapper.findOrderList(pageUtil);
        return new PageResult<>(orderList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    @Transactional
    public String updateOrderInfo(Order order) {
        Order temp = orderMapper.selectByPrimaryKey(order.getOrderId());
        // 不为空 且 orderStatus >= 0 且 状态为出库之前 可以修改部分信息（总价、地址）
        if (temp != null && temp.getOrderStatus() >= 0 && temp.getOrderStatus() < 3) {
            temp.setTotalPrice(order.getTotalPrice());
            temp.setUserAddress(order.getUserAddress());
            temp.setUpdateTime(new Date());
            if (orderMapper.updateByPrimaryKeySelective(temp) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            }
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    public boolean updateByPrimaryKeySelective(Order order) {
        return orderMapper.updateByPrimaryKeySelective(order) > 0;
    }

    @Override
    @Transactional
    public String checkDone(Long[] ids) {
        // 查询所有的订单 判断状态 修改状态和更新时间
        List<Order> orderList = orderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        List<String> errorOrderNoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderList)) {
            for (Order order : orderList) {
                if (order.getIsDeleted() == 1) { // 订单被删除
                    errorOrderNoList.add(order.getOrderNo());
                    continue;
                }
                if (order.getOrderStatus() != 1) { // 订单不是已支付
                    errorOrderNoList.add(order.getOrderNo());
                }
            }
            if (errorOrderNoList.isEmpty()) {
                // 所选的订单状态正常 可以执行配货完成操作 修改订单状态和更新时间
                if (orderMapper.checkDone(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                // 所选的订单此时不可执行出库操作
                if (errorOrderNoList.size() <= 5) {
                    return errorOrderNoList + "订单的状态不是支付成功无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功的订单，无法执行配货完成操作";
                }
            }
        }
        // 未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkOut(Long[] ids) {
        // 查询所有的订单 判断状态 修改状态和更新时间
        List<Order> orderList = orderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        List<String> errorOrderNoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderList)) {
            for (Order order : orderList) {
                if (order.getIsDeleted() == 1) {
                    errorOrderNoList.add(order.getOrderNo());
                    continue;
                }
                if (order.getOrderStatus() != 1 && order.getOrderStatus() != 2) {
                    errorOrderNoList.add(order.getOrderNo());
                }
            }
            if (errorOrderNoList.isEmpty()) {
                // 订单状态正常 可以执行出库操作 修改订单状态和更新时间
                if (orderMapper.checkOut(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                // 订单此时不可执行出库操作
                if (errorOrderNoList.size() <= 5) {
                    return errorOrderNoList + "订单的状态不是支付成功或配货完成无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功或配货完成的订单，无法执行出库操作";
                }
            }
        }
        // 未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String closeOrder(Long[] ids) {
        // 查询所有的订单 判断状态 修改状态和更新时间
        List<Order> orderList = orderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        List<String> errorOrderNoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderList)) {
            for (Order order : orderList) {
                // isDeleted=1 一定为已关闭订单
                if (order.getIsDeleted() == 1) {
                    errorOrderNoList.add(order.getOrderNo());
                    continue;
                }
                // 已关闭或者已完成 无法关闭订单
                if (order.getOrderStatus() == 4 || order.getOrderStatus() < 0) {
                    errorOrderNoList.add(order.getOrderNo());
                }
            }
            if (StringUtils.isEmpty(errorOrderNoList.toString())) {
                // 订单状态正常 可以执行关闭操作 修改订单状态和更新时间
                if (orderMapper.closeOrder(Arrays.asList(ids), OrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                // 订单此时不可执行关闭操作
                if (errorOrderNoList.size() <= 5) {
                    return errorOrderNoList + "订单不能执行关闭操作";
                } else {
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }
        // 未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrder(MallUserVO user, Long couponUserId, List<ShoppingCartItemVO> myShoppingCartItems) {
        // 购物车项目id表
        List<Long> itemIdList = myShoppingCartItems.stream()
                .map(ShoppingCartItemVO::getCartItemId).collect(Collectors.toList());
        // 商品id表
        List<Long> goodsIds = myShoppingCartItems.stream()
                .map(ShoppingCartItemVO::getGoodsId).collect(Collectors.toList());
        // 商品表
        List<GoodsInfo> goodsInfoList = goodsInfoMapper.selectByPrimaryKeys(goodsIds);
        // 检查是否包含已下架商品
        List<GoodsInfo> goodsListNotSelling = goodsInfoList.stream()
                .filter(goodsTemp -> goodsTemp.getGoodsSaleStatus() != Constants.SELL_STATUS_UP)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(goodsListNotSelling)) {
            // goodsListNotSelling 对象非空则表示有下架商品
            CMallException.fail(goodsListNotSelling.get(0).getGoodsName() + "已下架，无法生成订单");
        }
        // id 映射成GoodsInfo，相同id的合并
        Map<Long, GoodsInfo> goodsInfoMap = goodsInfoList.stream()
                .collect(Collectors.toMap(GoodsInfo::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
        // 判断商品库存
        for (ShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            // 查出的商品中不存在购物车中的这条关联商品数据，直接返回错误提醒
            if (!goodsInfoMap.containsKey(shoppingCartItemVO.getGoodsId())) {
                CMallException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
            }
            // 存在数量大于库存的情况，直接返回错误提醒
            if (shoppingCartItemVO.getGoodsCount() > goodsInfoMap.get(shoppingCartItemVO.getGoodsId()).getStockNum()) {
                CMallException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
            }
        }
        if (CollectionUtils.isEmpty(itemIdList) || CollectionUtils.isEmpty(goodsIds) || CollectionUtils.isEmpty(goodsInfoList)) {
            CMallException.fail(ServiceResultEnum.ORDER_GENERATE_ERROR.getResult());
        }
        // 购物车清空结算的项目
        if (shoppingCartItemMapper.deleteBatch(itemIdList) <= 0) {
            CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        // 更新库存
        List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(myShoppingCartItems, StockNumDTO.class);
        int updateStockNumResult = goodsInfoMapper.updateStockNum(stockNumDTOS);
        if (updateStockNumResult < 1) {
            CMallException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
        }
        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        int priceTotal = 0;
        // 保存订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(user.getUserId());
        order.setUserAddress(user.getAddress());
        // 总价
        for (ShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            priceTotal += shoppingCartItemVO.getGoodsCount() * shoppingCartItemVO.getSellingPrice();
        }
        // 如果使用了优惠券
        if (couponUserId != null) {
            UserCouponRecord userCouponRecord = userCouponRecordMapper.selectByPrimaryKey(couponUserId);
            Coupon coupon = couponMapper.selectByPrimaryKey(userCouponRecord.getCouponId());
            priceTotal -= coupon.getDiscount();
        }
        if (priceTotal < 1) {
            CMallException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
        }
        order.setTotalPrice(priceTotal);
        String extraInfo = "cmall-支付宝沙箱支付";
        order.setExtraInfo(extraInfo);
        // 生成订单并保存订单项纪录
        if (orderMapper.insertSelective(order) <= 0) {
            CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        // 如果使用了优惠券，则更新优惠券状态
        if (couponUserId != null) {
            UserCouponRecord userCouponRecord = new UserCouponRecord();
            userCouponRecord.setCouponUserId(couponUserId);
            userCouponRecord.setOrderId(order.getOrderId());
            userCouponRecord.setUseStatus((byte) 1);
            userCouponRecord.setUsedTime(new Date());
            userCouponRecord.setUpdateTime(new Date());
            userCouponRecordMapper.updateByPrimaryKeySelective(userCouponRecord);
        }
        // 生成所有的订单项快照，并保存至数据库
        List<OrderItem> orderItemList = new ArrayList<>();
        for (ShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            OrderItem orderItem = new OrderItem();
            // 使用BeanUtil工具类将newBeeMallShoppingCartItemVO中的属性复制到newBeeMallOrderItem对象中
            BeanUtil.copyProperties(shoppingCartItemVO, orderItem);
            // OrderMapper文件insert()方法中使用了useGeneratedKeys因此orderId可以获取到
            orderItem.setOrderId(order.getOrderId());
            orderItemList.add(orderItem);
        }
        // 保存至数据库
        if (orderItemMapper.insertBatch(orderItemList) <= 0) {
            CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        // 订单支付超期任务，超过300秒自动取消订单
        taskService.addTask(new OrderUnpaidTask(order.getOrderId(), ProjectConfig.getOrderUnpaidOverTime() * 1000));
        // 所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
        return orderNo;
    }

    @Override
    public String seckillSaveOrder(Long seckillSuccessId, Long userId) {
        SeckillSuccess seckillSuccess = seckillSuccessMapper.selectByPrimaryKey(seckillSuccessId);
        if (!seckillSuccess.getUserId().equals(userId)) {
            throw new CMallException("当前登陆用户与抢购秒杀商品的用户不匹配");
        }
        Long seckillId = seckillSuccess.getSeckillId();
        Seckill seckill = seckillMapper.selectByPrimaryKey(seckillId);
        Long goodsId = seckill.getGoodsId();
        GoodsInfo goodsInfo = goodsInfoMapper.selectByPrimaryKey(goodsId);
        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        // 保存订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setTotalPrice(seckill.getSeckillPrice());
        order.setUserId(userId);
        order.setUserAddress("秒杀测试地址");
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_PAID.getOrderStatus());
        order.setPayStatus((byte) PayStatusEnum.PAY_SUCCESSFUL.getPayStatus());
        order.setPayType((byte) PayTypeEnum.NOT_PAY.getPayType());
        order.setPayTime(new Date());
        String extraInfo = "";
        order.setExtraInfo(extraInfo);
        if (orderMapper.insertSelective(order) <= 0) {
            throw new CMallException("生成订单内部异常");
        }
        // 保存订单商品项
        OrderItem orderItem = new OrderItem();
        Long orderId = order.getOrderId();
        orderItem.setOrderId(orderId);
        orderItem.setSeckillId(seckillId);
        orderItem.setGoodsId(goodsInfo.getGoodsId());
        orderItem.setGoodsCoverImg(goodsInfo.getGoodsCoverImg());
        orderItem.setGoodsName(goodsInfo.getGoodsName());
        orderItem.setGoodsCount(1);
        orderItem.setSellingPrice(seckill.getSeckillPrice());
        if (orderItemMapper.insert(orderItem) <= 0) {
            throw new CMallException("生成订单内部异常");
        }
        // 订单支付超期任务
        taskService.addTask(new OrderUnpaidTask(order.getOrderId(), 30 * 1000));
        return orderNo;
    }

    @Override
    public OrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            CMallException.fail(ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult());
        }
        // 验证是否是当前userId下的订单，否则报错
        if (!userId.equals(order.getUserId())) {
            CMallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
        }
        // 获取订单项数据
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getOrderId());
        if (CollectionUtils.isEmpty(orderItems)) {
            CMallException.fail(ServiceResultEnum.ORDER_ITEM_NOT_EXIST_ERROR.getResult());
        }
        // 拷贝到订单项VOList
        List<OrderItemVO> orderItemVOList = BeanUtil.copyList(orderItems, OrderItemVO.class);
        // 订单VO
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        BeanUtil.copyProperties(order, orderDetailVO);
        orderDetailVO.setOrderStatusString(OrderStatusEnum.getOrderStatusEnumByStatus(orderDetailVO.getOrderStatus()).getName());
        orderDetailVO.setPaymentTypeString(PayTypeEnum.getPayTypeEnumByType(orderDetailVO.getPayType()).getName());
        orderDetailVO.setOrderItemVOList(orderItemVOList);
        return orderDetailVO;
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PageResult getMyOrders(PageQueryUtil pageUtil) {
        int total = orderMapper.getTotalOrders(pageUtil);
        List<OrderVO> orderVOList = new ArrayList<>();
        if (total > 0) {
            List<Order> orderList = orderMapper.findOrderList(pageUtil);
            // 数据转换 将实体类转成vo
            orderVOList = BeanUtil.copyList(orderList, OrderVO.class);
            // 设置订单状态显示值
            for (OrderVO orderVO : orderVOList) {
                orderVO.setOrderStatusString(OrderStatusEnum.getOrderStatusEnumByStatus(orderVO.getOrderStatus()).getName());
            }
            List<Long> orderIds = orderList.stream().map(Order::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)) {
                List<OrderItem> orderItems = orderItemMapper.selectByOrderIds(orderIds);
                // orderId->此order包含的item表
                Map<Long, List<OrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(OrderItem::getOrderId));
                for (OrderVO orderVO : orderVOList) {
                    // 封装每个订单列表对象的订单项数据
                    if (itemByOrderIdMap.containsKey(orderVO.getOrderId())) {
                        List<OrderItem> orderItemListTemp = itemByOrderIdMap.get(orderVO.getOrderId());
                        // 将OrderItem对象列表转换成OrderItemVO对象列表
                        List<OrderItemVO> orderItemVOList = BeanUtil.copyList(orderItemListTemp, OrderItemVO.class);
                        orderVO.setOrderItemVOList(orderItemVOList);
                    }
                }
            }
        }
        return new PageResult(orderVOList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String cancelOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            // 验证是否是当前userId下的订单，否则报错
            if (!userId.equals(order.getUserId())) {
                CMallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
            }
            // 订单状态判断
            if (order.getOrderStatus().intValue() == OrderStatusEnum.ORDER_SUCCESS.getOrderStatus()
                    || order.getOrderStatus().intValue() == OrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()
                    || order.getOrderStatus().intValue() == OrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus()
                    || order.getOrderStatus().intValue() == OrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            if (orderMapper.closeOrder(
                    Collections.singletonList(order.getOrderId()),
                    OrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String finishOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            // 验证是否是当前userId下的订单，否则报错
            if (!userId.equals(order.getUserId())) {
                return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
            }
            // 订单状态判断 非出库状态下不进行修改操作
            if (order.getOrderStatus().intValue() != OrderStatusEnum.ORDER_EXPRESS.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            order.setOrderStatus((byte) OrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            order.setUpdateTime(new Date());
            if (orderMapper.updateByPrimaryKeySelective(order) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String paySuccess(String orderNo, int payType) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
        }
        // 订单状态判断 非待支付状态下不进行修改操作
        if (order.getOrderStatus().intValue() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
        }
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_PAID.getOrderStatus());
        order.setPayType((byte) payType);
        order.setPayStatus((byte) PayStatusEnum.PAY_SUCCESSFUL.getPayStatus());
        order.setPayTime(new Date());
        order.setUpdateTime(new Date());
        if (orderMapper.updateByPrimaryKeySelective(order) <= 0) {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        // 支付成功会移除未支付队列的任务
        taskService.removeTask(new OrderUnpaidTask(order.getOrderId()));
        return ServiceResultEnum.SUCCESS.getResult();
    }

    @Override
    public List<OrderItemVO> getOrderItems(Long id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order != null) {
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getOrderId());
            // 获取订单项数据
            if (!CollectionUtils.isEmpty(orderItems)) {
                return BeanUtil.copyList(orderItems, OrderItemVO.class);
            }
        }
        return null;
    }
}