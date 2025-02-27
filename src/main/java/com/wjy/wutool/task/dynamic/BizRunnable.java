package com.wjy.wutool.task.dynamic;

import com.wjy.wutool.util.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 业务线程
 * 
 * @author weijiayu
 * @date 2024/10/9 17:52
 */
@Slf4j
public class BizRunnable<T> implements Runnable {

    private String beanName;
    private String methodName;
    private Class<?>[] classes;
    private Object[] params;

    public BizRunnable(String beanName, String methodName, Class<?>[] classes, Object[] params) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.classes = classes;
        this.params = params;
    }

    @Override
    public void run() {
        try {
            // 也可以直接硬编码，不用反射
            T service = SpringBeanUtil.getBean(beanName);
            Method method = service.getClass().getMethod(methodName, classes);
            method.invoke(service, params);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
}
