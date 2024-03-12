package com.hixtrip.sample.app.utils;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hixtrip.sample.app.strategy.PayResultAbstract;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ServiceLocatorUtils implements ApplicationContextAware {
    /**
     * 用于保存接口实现类名及对应的类
     */
    private Map<String, PayResultAbstract> map;

    /**
     * 获取应用上下文并获取相应的接口实现类
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //根据接口类型返回相应的所有bean
        Map<String, PayResultAbstract> map = applicationContext.getBeansOfType(PayResultAbstract.class);
    }

    public Map<String, PayResultAbstract> getMap() {
        return map;
    };

}