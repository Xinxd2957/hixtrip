#todo 你的建表语句,包含索引
CREATE TABLE order(
                      id VARCHAR(32) NOT NULL AUTO_INCREMENT  COMMENT '订单号' ,
                      user_id VARCHAR(255) NOT NULL   COMMENT '购买人' ,
                      sku_id VARCHAR(100)    COMMENT 'skuid' ,
                      amount INT    COMMENT '购买数量' ,
                      money DECIMAL(18,6)    COMMENT '购买金额' ,
                      merchant_id VARCHAR(255)    COMMENT '卖家ID',
                      pay_time DATETIME    COMMENT '购买时间' ,
                      pay_status VARCHAR(20)  default  '0'  COMMENT '支付状态 0待支付 1支付中 2支付成功 3 支付失败' ,
                      del_flag VARCHAR(255)  default  '0'  COMMENT '删除标志 0否 1 是' ,
                      revision VARCHAR(32)    COMMENT '乐观锁' ,
                      create_by VARCHAR(32)    COMMENT '创建人' ,
                      create_time DATETIME    COMMENT '创建时间' ,
                      update_by VARCHAR(32)    COMMENT '更新人' ,
                      update_time DATETIME    COMMENT '更新时间' ,
                      INDEX user_index (user_id),
                      INDEX merchant_index (merchant_id),
                      PRIMARY KEY (id)
)  COMMENT = '订单表';

-- 索引缘由：根据买家 和卖家  查询情况偏多，所以在买家id和卖家id的字段上添加索引

--   使用 Sharding-JDBC
--           <dependency>
--              <groupId>org.apache.shardingsphere</groupId>
--              <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
--              <version>4.1.1</version>
--           </dependency>

-- 策略 ： 水平切分模式（水平分库、分表）
--   方案1：根据 user_id%$  的策略去分区  user_id%24求余的策略
--  如 user_id = 49   49%24 =1 余数 1  则查询  order_1 的表
--  此方案下 买家查询自己的订单速度较快，但是卖家查询我的订单相对偏慢，
--  很可能有某些用户购买量偏多，有些用户偏少，有表数据量不平衡的情况


--        由于是电商系统，一般查询订单的数据都是在1年内的
-- 因此可以做冷热数据  1年内的数据做热数据，1年后-3年的冷数据   1+2的形式
-- 热数据库(1年) + 冷数据库(2-3年)  +历史数据库(3+)的形式
--   超出的则加入历史数据库基本不做查询。

