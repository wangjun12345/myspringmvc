package com.itlebron.demo.service.impl;

import com.itlebron.demo.service.DemoService;
import com.itlebron.springmvc.annotation.MyService;

/**
 * @Description:
 * @author: wangjun
 * @date: 2020/3/30
 **/
@MyService("demoService")
public class DemoServiceImpl implements DemoService {


    @Override
    public String query(String name) {
        return "My name is " + name;
    }
}
