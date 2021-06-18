package com.runtest;

import com.runtest.config.AppConfig;
import com.runtest.service.GoodServiceImpl;
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
