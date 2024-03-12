package com.hixtrip.sample.domain.order.repository;

import com.hixtrip.sample.domain.order.model.Order;

/**
 *
 */
public interface OrderRepository {

    /**
     * 创建订单
     * @param order
     * @return Order
     */
    public Order createOrder(Order order);

    /**
     * 修改订单
     * @param order
     * @return Order
     */
    public Order updateOrder(Order order);

    /**
     * 查询订单
     * @param id
     * @return
     */
    Order selectOrder(Long id);


}
