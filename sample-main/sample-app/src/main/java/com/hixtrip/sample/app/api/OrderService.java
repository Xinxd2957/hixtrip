package com.hixtrip.sample.app.api;

import com.hixtrip.sample.client.order.dto.CommandOderCreateDTO;
import com.hixtrip.sample.client.order.dto.CommandPayDTO;
import com.hixtrip.sample.client.order.vo.OrderVO;

import java.util.List;
import java.util.Map;

/**
 * 订单的service层
 */
public interface OrderService {


    OrderVO createOrder(CommandOderCreateDTO commandOderCreateDTO);

    /**
     * 模拟支付回调
     * @param commandPayDTO
     * @return
     */
    String payCallback(CommandPayDTO commandPayDTO);
}
