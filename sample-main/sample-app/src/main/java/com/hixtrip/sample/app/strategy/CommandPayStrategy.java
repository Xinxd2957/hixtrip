package com.hixtrip.sample.app.strategy;

import com.hixtrip.sample.domain.pay.model.CommandPay;

/**
 * 支付回调的策略接口
 */
public interface CommandPayStrategy {

    //收到回调后的处理方法
    Boolean payCallBack(CommandPay commandPay);

    //获取回调结果的名称
    String getPayResultName();
}
