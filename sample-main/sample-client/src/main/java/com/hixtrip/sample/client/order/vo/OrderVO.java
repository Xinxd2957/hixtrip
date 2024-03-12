package com.hixtrip.sample.client.order.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 订单返回
 */
@Data
@Builder
public class OrderVO {
    private String id;
    private String name;
    private String code;
    private String msg;
}
