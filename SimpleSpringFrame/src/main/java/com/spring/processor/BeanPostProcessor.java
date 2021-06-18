package com.spring.processor;

import com.sun.istack.internal.Nullable;

/**
 * 前置处理器
 * AOP的实现就是通过前置处理器实现的
 * 在Bean初始化过程中会调用具体实现方法,他不是针对某一个Bean才会调用，是所有Bean在初始化过程中都会调用
 *
 * @Author: tangwq
 */
public interface BeanPostProcessor {
    /**
     * 初始化前调用
     * @param bean
     * @param beanName
     * @return
     */
    @Nullable
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * 初始化后调用
     * @param bean
     * @param beanName
     * @return
     */
    @Nullable
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
