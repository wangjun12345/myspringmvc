package com.itlebron.springmvc;

import java.lang.reflect.Method;

/**
 * @Description:
 * @author: wangjun
 * @date: 2020/3/30
 **/
public class Handler {
    protected Object controller;
    protected Method method;

    public Handler(){

    }

    public Handler(Object controller, Method method){
        this.controller = controller;
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
