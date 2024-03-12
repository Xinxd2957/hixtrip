package com.hixtrip.sample.domain.order;

import com.hixtrip.sample.domain.commodity.CommodityDomainService;
import com.hixtrip.sample.domain.inventory.repository.InventoryRepository;
import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.order.repository.OrderRepository;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 订单领域服务
 * todo 只需要实现创建订单即可
 */
@Component
public class OrderDomainService {


    @Autowired
    OrderRepository orderRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    CommodityDomainService commodityDomainService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * todo 需要实现
     * 创建待付款订单
     */
    public Order createOrder(Order param) {
        /**
         * 根据SkuID查询商品价格 计算
         */
        BigDecimal skuPrice = commodityDomainService.getSkuPrice(param.getSkuId());

        //创建订单实体类 设置参数
        Order order = new Order();

        order.setUserId(param.getUserId());
        order.setSkuId(param.getSkuId());
        order.setAmount(param.getAmount());  //购买数量
        order.setMoney(skuPrice.multiply(new BigDecimal(param.getAmount())));  //购买金额
        order.setPayTime(LocalDateTime.now());
        order.setPayStatus("0");  //可不设置，数据库创建到时候默认设置0
        order.setDelFlag(0L);     //可不设置，数据库创建到时候默认设置0
        order.setCreateBy(param.getUserId());   //应数据库创建的时候就从权限那获取用户直接设置  修改同理
        order.setCreateTime(LocalDateTime.now()); //应数据库创建的时候直接设置  修改同理

        String merchantId  = ""; //可根据SKU 查询到卖家 赋值

        order.setMerchantId(merchantId);

        //TODO 需要你在infra实现, 自行定义出入参

        //调用创建订单接口
        Order orderCreate  = orderRepository.createOrder(param);

        /**
         * 通过id是否为0去判断创建是否成功，成功则id不为0
         *
         * 成功: 扣可销售库存 加占用库存
         * 失败: 返回
         */
        if(orderCreate.getId()!=0){
            //扣 - 可销售库存
            long sellableQuantity = inventoryRepository.getSellableQuantity(orderCreate.getSkuId()).longValue();
            long withholdingQuantity = inventoryRepository.getWithholdingQuantity(orderCreate.getSkuId()).longValue();
           Boolean sellResult = inventoryRepository.changeInventory(orderCreate.getSkuId(),
                   sellableQuantity - orderCreate.getAmount().longValue(),
                   withholdingQuantity + orderCreate.getAmount().longValue(),
                   orderCreate.getAmount().longValue());


            // 订单创建成功返回给前端，然后前端选择支付方式后，再去调用支付接口。
            // 在支付接口调用成功后，根据用户id，商品数量，商品金额等等生成一个key
            String redisOrderKey = "123";//模拟KEY
            redisTemplate.opsForValue().set("order:"+orderCreate.getUserId(),redisOrderKey,15, TimeUnit.SECONDS );
            //设置 X 秒内过期的key

            /**
             * 存入一个key后，下次支付时候再查询出来，如果kv还在或者一致则为重复支付
             */

            //如果扣库存失败那就把ID设为0  让请求失败
           if(!sellResult){
               orderCreate.setId(0L);
           }
        }
        return orderCreate;
    }

    /**
     * todo 需要实现
     * 待付款订单支付成功
     */
    public Boolean orderPaySuccess(CommandPay commandPay) {
        Order order = orderRepository.selectOrder(Long.valueOf(commandPay.getOrderId()));
        order.setPayStatus("2");  // 0待支付 1支付中 2支付完成 3 支付失败

        Order orderUpdate = orderRepository.updateOrder(order); //修改订单支付状态
        if(orderUpdate.getId()!=0){
            /**
             * 修改订单状态成功后去清除该订单的预占库存
             */
            long sellableQuantity = inventoryRepository.getSellableQuantity(orderUpdate.getSkuId()).longValue();
            long withholdingQuantity = inventoryRepository.getWithholdingQuantity(orderUpdate.getSkuId()).longValue();

            Boolean result = inventoryRepository.changeInventory(orderUpdate.getSkuId(),
                    sellableQuantity ,
                    withholdingQuantity - orderUpdate.getAmount().longValue(),
                    orderUpdate.getAmount().longValue());
            return true;
        }
        //需要你在infra实现, 自行定义出入参
        return false;
    }

    /**
     * todo 需要实现
     * 待付款订单支付失败
     */
    public Boolean orderPayFail(CommandPay commandPay) {
        //需要你在infra实现, 自行定义出入参

        Order order = orderRepository.selectOrder(Long.valueOf(commandPay.getOrderId()));
        order.setPayStatus("3");  // 0待支付 1支付中 2支付完成 3 支付失败

        Order orderUpdate = orderRepository.updateOrder(order); //修改订单支付状态
        if(orderUpdate.getId()!=0){
            /**
             * 修改订单状态成功后去清除加回可销售库存 和减预占库存
             */
            long sellableQuantity = inventoryRepository.getSellableQuantity(orderUpdate.getSkuId()).longValue();
            long withholdingQuantity = inventoryRepository.getWithholdingQuantity(orderUpdate.getSkuId()).longValue();

            Boolean result = inventoryRepository.changeInventory(orderUpdate.getSkuId(),
                    sellableQuantity+orderUpdate.getAmount().longValue() ,
                    withholdingQuantity - orderUpdate.getAmount().longValue(),
                    orderUpdate.getAmount().longValue());
            return true;
        }
        //需要你在infra实现, 自行定义出入参
        return false;
    }
}
