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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private OrderAddressMapper orderAddressMapper;

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
    public String updateOrderInfo(Order order) {
        Order temp = orderMapper.selectByPrimaryKey(order.getOrderId());
        // 不为空 且 orderStatus >= 0 且 状态为出库之前 可以修改部分信息（总价、地址）
        if (temp != null && temp.getOrderStatus() >= 0 && temp.getOrderStatus() < 3) {
            temp.setTotalPrice(order.getTotalPrice());
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
    public String saveOrder(MallUser user, Long couponUserId, UserAddress address, List<ShoppingCartItemVO> myShoppingCartItems) {
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
                .filter(goodsTemp -> goodsTemp.getGoodsSaleStatus() != Constants.SALE_STATUS_UP)
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
        // 保存订单
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(user.getUserId())
                .build();
        // 总价
        BigDecimal priceTotal = new BigDecimal(0);
        for (ShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            priceTotal = priceTotal
                    .add(shoppingCartItemVO.getSellingPrice().multiply(new BigDecimal(shoppingCartItemVO.getGoodsCount())))
                    .setScale(2, RoundingMode.HALF_UP) ;
        }
        // 如果使用了优惠券
        if (couponUserId != null) {
            // 查找领券记录
            UserCouponRecord userCouponRecord = userCouponRecordMapper.selectByPrimaryKey(couponUserId);
            Coupon coupon = couponMapper.selectByPrimaryKey(userCouponRecord.getCouponId());
            priceTotal = priceTotal.subtract(new BigDecimal(coupon.getDiscount()));
        }
        if (priceTotal.compareTo(new BigDecimal(1)) < 0) {
            CMallException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
        }
        order.setTotalPrice(priceTotal);
        String extraInfo = "cmall-支付宝沙箱支付";
        order.setExtraInfo(extraInfo);
        // 生成订单并保存订单纪录
        if (orderMapper.insertSelective(order) <= 0) {
            CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }

        // 如果使用了优惠券，则更新优惠券状态
        if (couponUserId != null) {
            UserCouponRecord userCouponRecord = UserCouponRecord.builder()
                    .couponUserId(couponUserId)
                    .orderId(order.getOrderId())
                    .useStatus((byte) 1)
                    .usedTime(new Date())
                    .updateTime(new Date())
                    .build();
            if (userCouponRecordMapper.updateByPrimaryKeySelective(userCouponRecord) <= 0){
                CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
            }
        }

        // 生成订单地址快照，并保存至数据库
        OrderAddress orderAddress = new OrderAddress();
        // 用户地址->订单地址
        BeanUtil.copyProperties(address, orderAddress);
        // OrderMapper文件insert()方法中使用了useGeneratedKeys因此orderId可以获取到
        orderAddress.setOrderId(order.getOrderId());
        // 保存订单地址快照至数据库
        if (orderAddressMapper.insertSelective(orderAddress) <= 0){
            CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }

        // 生成所有的订单项快照，并保存至数据库
        List<OrderItem> orderItemList = new ArrayList<>();
        for (ShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            OrderItem orderItem = new OrderItem();
            // 使用BeanUtil工具类将ShoppingCartItemVO中的属性复制到OrderItem对象中
            BeanUtil.copyProperties(shoppingCartItemVO, orderItem);
            // OrderMapper文件insert()方法中使用了useGeneratedKeys因此orderId可以获取到
            orderItem.setOrderId(order.getOrderId());
            orderItemList.add(orderItem);
        }
        // 保存订单项快照至数据库
        if (orderItemMapper.insertBatch(orderItemList) <= 0) {
            CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }

        // 订单支付超期任务，超过300秒自动取消订单
        taskService.addTask(new OrderUnpaidTask(order.getOrderId(), ProjectConfig.getOrderUnpaidOverTime() * 1000));
        // 所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
        return orderNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String seckillSaveOrder(Long seckillSuccessId, Long userId, UserAddress address) {
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
        Order order = Order.builder()
                .orderNo(orderNo)
                .totalPrice(seckill.getSeckillPrice())
                .userId(userId)
                .orderStatus((byte) OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus())
                .build();
        String extraInfo = "";
        order.setExtraInfo(extraInfo);
        if (orderMapper.insertSelective(order) <= 0) {
            throw new CMallException("生成秒杀订单内部异常");
        }

        // 生成订单地址快照，并保存至数据库
        OrderAddress orderAddress = new OrderAddress();
        // 用户地址->订单地址
        BeanUtil.copyProperties(address, orderAddress);
        orderAddress.setOrderId(order.getOrderId());
        // 保存订单地址快照至数据库
        if (orderAddressMapper.insertSelective(orderAddress) <= 0){
            CMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }

        // 保存订单商品项
        OrderItem orderItem = OrderItem.builder()
                .orderId(order.getOrderId())
                .seckillId(seckillId)
                .goodsId(goodsInfo.getGoodsId())
                .goodsCoverImg(goodsInfo.getGoodsCoverImg())
                .goodsCount(1)
                .sellingPrice(seckill.getSeckillPrice())
                .build();
        if (orderItemMapper.insert(orderItem) <= 0) {
            throw new CMallException("生成秒杀订单内部异常");
        }
        // 订单支付超期任务
        taskService.addTask(new OrderUnpaidTask(order.getOrderId(), ProjectConfig.getSeckillOrderUnpaidOverTime() * 1000));
        return orderNo;
    }

    @Override
    public OrderDetailVO getOrderDetailByOrderId(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            CMallException.fail(ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult());
        }
        return genOrderDetailVO(order);
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
        return genOrderDetailVO(order);
    }

    /**
     * 生成订单细节VO
     * 细节中要展示每个订单项的实际付款
     * */
    private OrderDetailVO genOrderDetailVO(Order order){
        // 获取订单项数据
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getOrderId());
        if (CollectionUtils.isEmpty(orderItems)) {
            CMallException.fail(ServiceResultEnum.ORDER_ITEM_NOT_EXIST_ERROR.getResult());
        }
        // 拷贝到订单项VOList
        List<OrderItemVO> orderItemVOList = BeanUtil.copyList(orderItems, OrderItemVO.class);
        OrderAddress orderAddress = orderAddressMapper.selectByPrimaryKey(order.getOrderId());
        if (orderAddress == null){
            CMallException.fail(ServiceResultEnum.ORDER_ADDRESS_NULL_ERROR.getResult());
        }
        // 订单VO
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        BeanUtil.copyProperties(order, orderDetailVO);
        orderDetailVO.setOrderStatusString(OrderStatusEnum.getOrderStatusEnumByStatus(orderDetailVO.getOrderStatus()).getName());
        orderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(orderDetailVO.getPayType()).getName());
        orderDetailVO.setUserAddress(orderAddress.toString());
        // 计算每个订单项的实际付款价格
        calPaidPrice(order, orderItemVOList);
        orderDetailVO.setOrderItemVOList(orderItemVOList);

        // 优惠券信息
        UserCouponRecord userCouponRecord = userCouponRecordMapper.getUserCouponByOrderId(order.getOrderId());
        if (userCouponRecord != null){
            Coupon coupon = couponMapper.selectByPrimaryKey(userCouponRecord.getCouponId());
            orderDetailVO.setDiscount(new BigDecimal(coupon.getDiscount()));
        }

        return orderDetailVO;
    }


    private void calPaidPrice(Order order, List<OrderItemVO> orderItemVOList){
        BigDecimal paidTotal = order.getTotalPrice();
        BigDecimal totalPrice = orderItemVOList.stream()
                .map(e -> e.getSellingPrice().multiply(new BigDecimal(e.getGoodsCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        for (OrderItemVO orderItemVO : orderItemVOList) {
            BigDecimal sellingPrice = orderItemVO.getSellingPrice().multiply(new BigDecimal(orderItemVO.getGoodsCount()));
            if (paidTotal.compareTo(totalPrice) == 0){
                orderItemVO.setPaidPrice(sellingPrice);
            }else{
                orderItemVO.setPaidPrice(sellingPrice.divide(totalPrice,2,RoundingMode.HALF_UP).multiply(paidTotal));
            }
        }
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
                List<OrderItem> orderItemList = orderItemMapper.selectByOrderIds(orderIds);
                // orderId->此order包含的item表
                Map<Long, List<OrderItem>> itemByOrderIdMap = orderItemList.stream().collect(groupingBy(OrderItem::getOrderId));
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
    @Transactional
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
            //修改订单状态 && 恢复库存
            if (orderMapper.closeOrder(
                    Collections.singletonList(order.getOrderId()),
                    OrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0
            && recoverStockNum(Collections.singletonList(order.getOrderId()))) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String deleteOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null){
            // 验证是否是当前userId下的订单，否则报错
            if (!userId.equals(order.getUserId())) {
                return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
            }
            // 订单状态判断，如果不是关闭或完成的状态则报错
            if (order.getOrderStatus().intValue() >= 0
                    && order.getOrderStatus().intValue() != OrderStatusEnum.ORDER_SUCCESS.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            order.setIsDeleted((byte) 1);
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
        // 虽然是new 但是只要订单id相同，他们就是相同的任务
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

    /**
     * @Description 恢复库存
     * @Param [orderIds]
     * @Return java.lang.Boolean
     */
    private Boolean recoverStockNum(List<Long> orderIds) {
        //查询对应的订单项
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderIds(orderIds);
        //获取对应的商品id和商品数量并赋值到StockNumDTO对象中
        List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(orderItemList, StockNumDTO.class);
        //执行恢复库存的操作
        int updateStockNumResult = goodsInfoMapper.recoverStockNum(stockNumDTOS);
        if (updateStockNumResult < 1) {
            CMallException.fail(ServiceResultEnum.CLOSE_ORDER_ERROR.getResult());
            return false;
        } else {
            return true;
        }
    }
}
