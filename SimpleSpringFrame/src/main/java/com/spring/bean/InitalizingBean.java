package com.spring.bean;

/**
 * 初始化器
 * 初始化Bean的一个接口，对完成实例化，属性注入的bean进行初始化
 * 只针对实现了这个接口的bean，就在初始化过程中调用里面的方法
 *
 * @Author: tangwq
 */
public interface InitalizingBean {

    void afterPropertiesSet() throws Exception;
}
