package com.hixtrip.sample.app.strategy;

import com.hixtrip.sample.domain.order.OrderDomainService;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaySuccess extends PayResultAbstract {

    @Autowired
    OrderDomainService orderDomainService;
    @Override
    public Boolean payCallBack(CommandPay commandPay) {
        return orderDomainService.orderPaySuccess(commandPay);
    }

    @Override
    public String getPayResultName() {
        //模拟返回值
        return "WeChat_Pay_SUCCESS";
    }
}
