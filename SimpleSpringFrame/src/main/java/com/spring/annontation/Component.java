package com.spring.annontation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 实现Spirng中 bean注解Component
 * @Author: tangwq
 */

@Retention(RetentionPolicy.RUNTIME) //元注解，指明定义注解的生命周期
@Target(ElementType.TYPE) //元注解  指明该注解可以修饰哪些元素
public @interface Component {
    /**
     * 里面定义注解的参数，无默认值则使用注解时必须传入
     * @return
     */
    // 扫描的路径值
    String value() default "";
}
