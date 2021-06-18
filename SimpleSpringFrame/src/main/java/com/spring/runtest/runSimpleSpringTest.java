package com.spring.runtest;

import com.spring.runtest.config.AppConfig;
import com.spring.runtest.service.GoodServiceImpl;
import com.spring.context.SimpleApplicationContext;

/**
 * @Author: tangwq
 */
public class runSimpleSpringTest {
    public static void main(String[] args) {
        SimpleApplicationContext simpleApplicationContext = new SimpleApplicationContext(AppConfig.class);
        GoodServiceImpl goodServiceImpl = (GoodServiceImpl) simpleApplicationContext.getBean("goodService");
        goodServiceImpl.test();
    }
}
