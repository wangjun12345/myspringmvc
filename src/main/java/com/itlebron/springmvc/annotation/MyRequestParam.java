package com.itlebron.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @Description:
 * @author: wangjun
 * @date: 2020/3/30
 **/


@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {

    String value() default "";

}
