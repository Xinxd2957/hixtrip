package com.hixtrip.sample.infra;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.order.repository.OrderRepository;
import com.hixtrip.sample.domain.sample.model.Sample;
import com.hixtrip.sample.infra.db.convertor.OrderDOConvertor;
import com.hixtrip.sample.infra.db.convertor.SampleDOConvertor;
import com.hixtrip.sample.infra.db.dataobject.OrderDO;
import com.hixtrip.sample.infra.db.dataobject.SampleDO;
import com.hixtrip.sample.infra.db.mapper.OrderMapper;
import lombok.Data;
import org.apache.catalina.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderRepositoryImpl implements OrderRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private OrderMapper orderMapper;


    /**
     * 枚举类  是否删除的枚举
     */
    enum delFlagNum{

        USE(0,"使用中"),

        DEL(1,"已删除");


        private Integer code;
        private String msg;

        delFlagNum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

    /**
     * 创建订单
     * @param param
     * @return Order
     */
    @Override
    public Order createOrder(Order param) {

        /**
         * 往数据库插入订单对象  成功的话会把表的主键id返回到id
         */
        OrderDO orderDO = OrderDOConvertor.INSTANCE.domainToDo(param);

        /**
         * 如果成功会返回插入成功的条数，此处为1， 失败为0
         */
        int result = orderMapper.insert(orderDO);
        if(result>0){
            // 成功则通过返回的主键id，再次查询订单，返回
            OrderDO findOrder = orderMapper.selectOne(
                    new QueryWrapper<OrderDO>().lambda()
                            .eq(OrderDO::getId,orderDO.getId())
                            .eq(OrderDO::getDelFlag,delFlagNum.USE.code));

            return OrderDOConvertor.INSTANCE.doToDomain(findOrder);
        }
        orderDO.setId(0L);  //用id是否为0去判断是否创建成功
        return OrderDOConvertor.INSTANCE.doToDomain(orderDO);
    }


    /**
     * 修改
     * @param param
     * @return Order
     */
    @Override
    public Order updateOrder(Order param){
        OrderDO orderDO = OrderDOConvertor.INSTANCE.domainToDo(param);

        /**
         * 此处应有判空操作 略
         */
        /**
         * 如果成功会返回插入成功的条数，此处为1， 失败为0
         */
        int result = orderMapper.updateById(orderDO);
        if(result>0){
            OrderDO findOrder = orderMapper.selectOne(
                    new QueryWrapper<OrderDO>().lambda()
                            .eq(OrderDO::getId,orderDO.getId()));

            return OrderDOConvertor.INSTANCE.doToDomain(findOrder);
        }
        orderDO.setId(0L);  //用id是否为0去判断是否创建成功
        return OrderDOConvertor.INSTANCE.doToDomain(orderDO);

    }

    /**
     * 查询订单
     * @param id
     * @return
     */
    @Override
    public Order selectOrder(Long id){
        return  OrderDOConvertor.INSTANCE.doToDomain(orderMapper.selectById(id));
    }
}
