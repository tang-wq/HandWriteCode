package com.spring.annontation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean作用域注解 （单例，多例）
 * @Author: tangwq
 */
@Retention(RetentionPolicy.RUNTIME) //元注解，指明定义注解的生命周期
@Target(ElementType.TYPE) //元注解  指明该注解可以修饰哪些元素
public @interface Scope {

    //默认值为单例
    String value() default "protoType";
}
