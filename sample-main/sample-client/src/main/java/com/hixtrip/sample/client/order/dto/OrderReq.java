package com.hixtrip.sample.client.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询请求的入参
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderReq {

    /**
     * 订单id
     */
    private String orderId;



}
