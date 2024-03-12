package com.hixtrip.sample.client.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改库存的请求 入参
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryUpdateDTO {


    /**
     * 商品规格id
     */
    private String skuId;

    /**
     * 可售库存
     */
    private Long sellableQuantity;

    /**
     * 预占库存
     */
    private Long withholdingQuantity;

    /**
     * 占用库存
     */
    private Long occupiedQuantity;



}
