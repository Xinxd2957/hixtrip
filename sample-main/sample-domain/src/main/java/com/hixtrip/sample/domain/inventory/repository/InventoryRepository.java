package com.hixtrip.sample.domain.inventory.repository;

/**
 *
 */
public interface InventoryRepository {



    /**
     * 获取sku当前库存 --可售库存
     *
     * @param skuId
     */
    Integer getSellableQuantity(String skuId);

    /**
     * 获取占用库存
     *
     * @param skuId
     */
    Integer getWithholdingQuantity(String skuId);

    /**
     * 修改库存
     *
     * @param skuId
     * @param sellableQuantity    可售库存
     * @param withholdingQuantity 预占库存
     * @param occupiedQuantity    占用库存
     * @return
     */
    Boolean changeInventory(String skuId, Long sellableQuantity, Long withholdingQuantity, Long occupiedQuantity);
}
