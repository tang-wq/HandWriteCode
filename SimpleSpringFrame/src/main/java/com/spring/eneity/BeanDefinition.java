package com.spring.eneity;

/**
 * 一个简化版的BeanDefination对象
 * @Author: tangwq
 */
public class BeanDefinition {

    private Class clazz;
    //作用域
    private String scope;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
