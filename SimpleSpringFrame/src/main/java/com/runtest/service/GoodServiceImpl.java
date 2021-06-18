package com.runtest.service;

import com.spring.annontation.Autowired;
import com.spring.annontation.Component;

/**
 * @Author: tangwq
 */
@Component("goodService")
public class GoodServiceImpl implements GoodsService {

    @Autowired
    UserServiceImpl userServiceImpl;

    public void test(){
        System.out.println(userServiceImpl);
    }
}
