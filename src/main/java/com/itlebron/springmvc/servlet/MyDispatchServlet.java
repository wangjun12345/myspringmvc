package com.itlebron.springmvc.servlet;

import com.itlebron.springmvc.Handler;
import com.itlebron.springmvc.annotation.MyAutowired;
import com.itlebron.springmvc.annotation.MyController;
import com.itlebron.springmvc.annotation.MyRequestMapping;
import com.itlebron.springmvc.annotation.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @Description: 前端控制器
 * @author: wangjun
 * @date: 2020/3/30
 **/
public class MyDispatchServlet extends HttpServlet {

    /**
     * 配置文件properties
     * */
    private Properties contextConfig = new Properties();

    /**
     * 所有的className
     * */
    private List<String> classNameList = new ArrayList<>();

    /**
     * IOC容器
     * */
    private Map<String, Object> ioc = new HashMap<>();

    /**
     * handlerMapping的集合
     * */
    private Map<String, Handler> handlerMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6.处理请求
        try{
            doDispatch(req, resp);
        }catch (Exception e){
            e.printStackTrace();
            resp.getWriter().write("500！service error！");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        //请求的全路径
        String url = req.getRequestURI();
        //路径前缀
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "");
        //如果handlermapping中没有，则报404
        if(!handlerMapping.containsKey(url)){
            resp.getWriter().write("404！request not found！");
            return;
        }
        Handler handler = handlerMapping.get(url);
        Method method = handler.getMethod();
        method.invoke(handler.getController(), req, resp);

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2.扫描所有相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        //3.实例化所有相关的类
        doInstance();
        //4.自动注入
        doDI();
        //5.初始化HandlerMapping
        initHandlerMapping();


    }

    private void initHandlerMapping() {
        //判断ioc是否为空
        if(ioc.isEmpty()){
            return;
        }
        //遍历赋值
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class clazz = entry.getValue().getClass();
            //是controller类下才有handler
            if (clazz.isAnnotationPresent(MyController.class)){
                String baseUrl = "";
                if (clazz.isAnnotationPresent(MyRequestMapping.class)){//取出controller上的requestMapping的值
                    MyRequestMapping requestMapping = (MyRequestMapping) clazz.getAnnotation(MyRequestMapping.class);
                    baseUrl = requestMapping.value();
                }
                //遍历类中所有的方法
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    //如果方法有MyRequestMapping注解，这个方法要加到HandlerMappingMap中
                    if (method.isAnnotationPresent(MyRequestMapping.class)){
                        MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                        String methodUrl = ("/" + baseUrl + requestMapping.value()).replaceAll("/+", "/");
                        handlerMapping.put(methodUrl, new Handler(entry.getValue(), method));
                    }
                }

            }

        }

    }

    private void doDI() {
        //是否为空
        if(ioc.isEmpty()){
            return;
        }
        //遍历注入
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //得到类的所有属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                //是否加了@MyAutowired注解，加了就要注入
                if (field.isAnnotationPresent(MyAutowired.class)){
                    //取出注解上定义的bean引用
                    MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                    String beanName = autowired.value();
                    if ("".equals(beanName)){
                        beanName = field.getType().getName();//注解未定义，则取出引用类的类名
                    }
                    field.setAccessible(true);//允许访问
                    try {
                        field.set(entry.getValue(), ioc.get(beanName));//todo
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }

                }
            }
            
            
        }

    }

    private void doInstance() {
        //是否为空
        if(classNameList.size() == 0){
            return;
        }
        //遍历实例化相关类，并加载到ioc容器中
        for (String className : classNameList) {
            try {
                Class clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(MyController.class)){//如果是Controller注解
                    /*ioc容器的beanName，优先采用注解中定义的，未定义则默认类名首字母小写*/
                    //先查看是否在注解里定义了
                    MyController controller = (MyController) clazz.getAnnotation(MyController.class);
                    String beanName = controller.value();
                    if("".equals(beanName)){
                        beanName = clazz.getName();//首字母小写，作为ioc容器的key
                    }
                    //添加到ioc容器
                    ioc.put(beanName, clazz.newInstance());
                }else if (clazz.isAnnotationPresent(MyService.class)){
                    /*ioc容器的beanName，优先采用注解中定义的，未定义则默认类名首字母小写*/
                    //先查看是否在注解里定义了
                    MyService service = (MyService) clazz.getAnnotation(MyService.class);
                    String beanName = service.value();
                    if("".equals(beanName)){
                        beanName = lowerFirstChar(clazz.getName());//首字母小写，作为ioc容器的key
                    }
                    //添加到ioc容器
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //实现了接口，接口也加到ioc容器,key是接口名，value还是这个实现类
                    for (Class anInterface : clazz.getInterfaces()) {
                        ioc.put(anInterface.getName(), instance);
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    private void doScanner(String scanPackage) {
        //通过包名com.itlebron.springmvc转化为/com/itlebron/springmvc
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classFile = new File(url.getFile());
        File[] files = classFile.listFiles();//包下所有文件
        //遍历
        for (File file : files) {
            //判断是不是目录
            if(file.isDirectory()){
                //是目录递归扫描该目录
                doScanner(scanPackage + "." + file.getName());
            }else{
                //将所有的类都加到classNameList中
                String  className = scanPackage + "." + file.getName().replace(".class", "");
                classNameList.add(className);
            }
        }

    }

    private void doLoadConfig(String contextConfigLocation) {
        //加载配置文件
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 字符串首字母大写变为小写
     * */
    private String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.copyValueOf(chars);
    }
}
