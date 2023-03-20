package com.tsong.cmall.service;

import com.tsong.cmall.controller.vo.MallUserVO;
import com.tsong.cmall.controller.vo.OrderDetailVO;
import com.tsong.cmall.controller.vo.OrderItemVO;
import com.tsong.cmall.controller.vo.ShoppingCartItemVO;
import com.tsong.cmall.entity.Order;
import com.tsong.cmall.util.PageQueryUtil;
import com.tsong.cmall.util.PageResult;

import java.util.List;

public interface OrderService {
    PageResult<Order> getNewBeeMallOrdersPage(PageQueryUtil pageUtil);

    /**
     * @Description 订单信息修改
     * @Param [order]
     * @Return java.lang.String
     */

    String updateOrderInfo(Order order);

    /**
     * @Description 根据主键修改订单信息
     * @Param [order]
     * @Return boolean
     */

    boolean updateByPrimaryKeySelective(Order order);

    /**
     * @Description 配货
     * @Param [ids]
     * @Return java.lang.String
     */

    String checkDone(Long[] ids);

    /**
     * @Description 出库
     * @Param [ids]
     * @Return java.lang.String
     */

    String checkOut(Long[] ids);

    /**
     * @Description 关闭订单
     * @Param [ids]
     * @Return java.lang.String
     */

    String closeOrder(Long[] ids);

    /**
     * @Description 保存订单
     * @Param [user, couponUserId, myShoppingCartItems]
     * @Return java.lang.String
     */

    String saveOrder(MallUserVO user, Long couponUserId, List<ShoppingCartItemVO> myShoppingCartItems);

    /**
     * @Description 生成秒杀订单
     * @Param [seckillSuccessId, userId]
     * @Return java.lang.String
     */

    String seckillSaveOrder(Long seckillSuccessId, Long userId);

    /**
     * @Description 获取订单详情，用于返回前端
     * @Param [orderNo, userId]
     * @Return com.tsong.cmall.controller.vo.OrderDetailVO
     */
    OrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId);

    /**
     * @Description 获取订单详情
     * @Param [orderNo]
     * @Return com.tsong.cmall.entity.Order
     */
    Order getOrderByOrderNo(String orderNo);

    /**
     * @Description 我的订单列表
     * @Param [pageUtil]
     * @Return com.tsong.cmall.util.PageResult
     */
    PageResult getMyOrders(PageQueryUtil pageUtil);

    /**
     * @Description 手动取消订单
     * @Param [orderNo, userId]
     * @Return java.lang.String
     */
    String cancelOrder(String orderNo, Long userId);

    /**
     * @Description 确认收货
     * @Param [orderNo, userId]
     * @Return java.lang.String
     */
    String finishOrder(String orderNo, Long userId);

    /**
     * @Description 支付成功
     * @Param [orderNo, payType]
     * @Return java.lang.String
     */
    String paySuccess(String orderNo, int payType);

    /**
     * @Description 获得订单项目
     * @Param [id]
     * @Return java.util.List<com.tsong.cmall.controller.vo.OrderItemVO>
     */
    List<OrderItemVO> getOrderItems(Long id);
}
