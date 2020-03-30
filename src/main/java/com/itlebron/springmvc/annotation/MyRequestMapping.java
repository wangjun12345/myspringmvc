package com.itlebron.springmvc.annotation;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * @Description:
 * @author: wangjun
 * @date: 2020/3/30
 **/


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {

    String value() default "";

}
