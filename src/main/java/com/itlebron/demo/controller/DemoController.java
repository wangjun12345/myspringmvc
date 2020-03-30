package com.itlebron.demo.controller;

import com.itlebron.demo.service.DemoService;
import com.itlebron.springmvc.annotation.MyAutowired;
import com.itlebron.springmvc.annotation.MyController;
import com.itlebron.springmvc.annotation.MyRequestMapping;
import com.itlebron.springmvc.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description:
 * @author: wangjun
 * @date: 2020/3/30
 **/
@MyController
@MyRequestMapping("/demo")
public class DemoController {

    @MyAutowired
    private DemoService demoService;

    @MyRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response, @MyRequestParam("name") String name) throws IOException {
        String result = demoService.query(name);
        response.getWriter().write(result);
    }

    @MyRequestMapping("/index")
    public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String result = demoService.query("index");
        response.getWriter().write(result);
    }

}
