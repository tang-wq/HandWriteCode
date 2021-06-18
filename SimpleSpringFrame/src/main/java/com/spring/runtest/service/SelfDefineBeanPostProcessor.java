package com.spring.runtest.service;

import com.spring.annontation.Component;
import com.spring.processor.BeanPostProcessor;

/**
 * @Author: tangwq
 */
@Component
public class SelfDefineBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("前置处理器之before方法");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("前置处理器之after方法");
        return bean;
    }


}
