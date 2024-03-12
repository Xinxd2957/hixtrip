package com.hixtrip.sample.infra;

import com.hixtrip.sample.domain.inventory.repository.InventoryRepository;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * infra层是domain定义的接口具体的实现
 */
@Component
public class InventoryRepositoryImpl implements InventoryRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    Redisson redisson;

    /**
     * 缓存key应以某种方式统一定义，这里模拟直接创建
     *
     * 格式：
     *    例如skuId为 1000 的商品
     *    inventory:1000:sellableQuantity 82   -可销售库存
     *    inventory:1000:occupiedQuantity 18   -占用库存
     */

    String keyBefore = "inventory";  //模拟库存在缓存中的key

    String sellableQuantityBefore = "sellableQuantity";  //模拟可售库存在缓存中的key

    String withholdingQuantityBefore = "withholdingQuantity";  //模拟预占库存在缓存中的key

    /**
     * 拼接可售库存Key
     * @param skuId
     * @return String
     */
    private String getSellableQuantityKey(String skuId){
        return keyBefore+":"+skuId+":"+sellableQuantityBefore;
    }

    /**
     * 拼接占用库存Key
     * @param skuId
     * @return String
     */
    private String getWithholdingQuantityKey(String skuId){
        return keyBefore+":"+skuId+":"+withholdingQuantityBefore;
    }



    /**
     * 获取sku当前库存 --可售库存
     *
     * @param skuId
     */
    @Override
    public Integer getSellableQuantity(String skuId) {
        String skuKey =getSellableQuantityKey(skuId);  //模拟sku商品库存在缓存中的key  --可售库存
        return Integer.parseInt(redisTemplate.opsForValue().get(skuKey).toString());

    }

    /**
     * 获取占用库存
     *
     * @param skuId
     */
    @Override
    public Integer getWithholdingQuantity(String skuId) {
        String skuKey =getWithholdingQuantityKey(skuId);  //模拟sku商品库存在缓存中的key -- withholdingQuantity
        return Integer.parseInt(redisTemplate.opsForValue().get(skuKey).toString());

    }

    /**
     * 修改库存
     *
     * @param skuId
     * @param sellableQuantity    可售库存
     * @param withholdingQuantity 预占库存
     * @param occupiedQuantity    占用库存
     * @return
     */
    @Override
    public Boolean changeInventory(String skuId, Long sellableQuantity, Long withholdingQuantity, Long occupiedQuantity) {

        String lockKey = "lockKey";

        // 获取到redisson锁对象
        RLock redissonLock = redisson.getLock(lockKey);
        try {
            // ========= 添加redisson锁并实现锁续命功能 =============
            /**
             *  主要执行一下几个操作
             *
             *  1、将localKey设置到Redis服务器上，默认过期时间是30s
             *  2、每10s触发一次锁续命功能
             */
            redissonLock.lock();

            // ======== 扣减库存业务员开始 ============
                redisTemplate.opsForValue().set(getSellableQuantityKey(skuId), sellableQuantity + "");
                System.out.println("修改成功，剩余可销库存：" + sellableQuantity + "");

                redisTemplate.opsForValue().set(getWithholdingQuantityKey(skuId), withholdingQuantity + "");
                System.out.println("修改成功，剩余占用库存：" + withholdingQuantity + "");

            return true;
            // ======== 扣减库存业务员结束 ============
        } finally { // 防止异常导致锁无法释放！！！
            // ============= 释放redisson锁 ==========
            redissonLock.unlock();
        }
    }


//    /**
//     * 修改预占库存
//     *
//     * @param skuId
//     * @param withholdingQuantity 预占库存
//     * @param occupiedQuantity    占用库存
//     * @return
//     */
//    @Override
//    public Boolean changewithholdingQuantity(String skuId, Long withholdingQuantity, Long occupiedQuantity) {
//
//        String lockKey = "lockKey";
//
//        // 获取到redisson锁对象
//        RLock redissonLock = redisson.getLock(lockKey);
//        try {
//            // ========= 添加redisson锁并实现锁续命功能 =============
//            /**
//             *  主要执行一下几个操作
//             *
//             *  1、将localKey设置到Redis服务器上，默认过期时间是30s
//             *  2、每10s触发一次锁续命功能
//             */
//            redissonLock.lock();
//
//            // ======== 扣减库存业务员开始 ============
//
//            //1. 从redis获取可售库存数量
//            int stock = getWithholdingQuantity(skuId);
//            // 如果扣除后库存数量大于0
//            int realStock =stock -occupiedQuantity.intValue();
//
//            if (realStock >= 0) {
//                // 相当于jedis.set(key, value)
//                redisTemplate.opsForValue().set(getWithholdingQuantityKey(skuId), realStock + "");
//                System.out.println("修改成功，剩余预占库存：" + realStock + "");
//                return true;
//            } else { // 如果库存数量小于0
//                System.out.println("修改失败，剩余预占库存不足！");
//                return false;
//            }
//
//            // ======== 扣减库存业务员结束 ============
//        } finally { // 防止异常导致锁无法释放！！！
//            // ============= 释放redisson锁 ==========
//            redissonLock.unlock();
//        }
//    }

}
