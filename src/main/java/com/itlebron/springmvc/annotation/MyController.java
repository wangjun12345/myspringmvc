package com.itlebron.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @Description:
 * @author: wangjun
 * @date: 2020/3/30
 **/


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {

    String value() default "";

}
