package com.wjy.wutool.task.dynamic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.support.CronExpression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态定时任务
 * 
 * @author weijiayu
 * @date 2024/3/21 15:22
 */
@Slf4j
public class DynamicTaskManager<T> implements DisposableBean {

    private ThreadPoolTaskScheduler taskScheduler;

    private Map<String, ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap<>();

    private String beanName;
    private String methodName;
    private Class<?>[] classes;

    public DynamicTaskManager(String beanName, String methodName, Class<?>[] classes,
        ThreadPoolTaskScheduler taskScheduler) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.classes = classes;
        this.taskScheduler = taskScheduler;
    }

    public DynamicTaskManager(String beanName, String methodName, Class<?>[] classes, String threadNamePrefix) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.classes = classes;

        this.taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setRemoveOnCancelPolicy(true);
        taskScheduler.setAwaitTerminationSeconds(10);
        taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        taskScheduler.setThreadNamePrefix(threadNamePrefix);
        taskScheduler.initialize();
    }

    /** 增加或更新任务 */
    public boolean addOrUpdTask(String id, String cronExpression, Object[] params) {
        if (!CronExpression.isValidExpression(cronExpression)) {
            return false;
        }
        ScheduledFuture future = scheduledFutureMap.get(id);
        // 原来如果有任务，先终止旧的
        if (future != null && !future.cancel(true)) {
            return false;
        }
        CronTask cronTask = new CronTask(new BizRunnable<T>(beanName, methodName, classes, params), cronExpression);
        future = taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger());
        scheduledFutureMap.put(id, future);
        return true;
    }

    /** 删除任务 */
    public boolean deleteTask(String id) {
        ScheduledFuture future = scheduledFutureMap.get(id);
        if (future == null) {
            return true;
        }
        if (future.cancel(true)) {
            return scheduledFutureMap.remove(id, future);
        } else {
            return false;
        }
    }

    @Override
    public void destroy() throws Exception {
        // bean销毁时优雅关闭
        for (ScheduledFuture future : this.scheduledFutureMap.values()) {
            future.cancel(true);
        }
        scheduledFutureMap.clear();
        taskScheduler.shutdown();
    }
}
