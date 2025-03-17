package com.wjy.wutool.task;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 并行执行任务工具类。阻塞等待所有任务都执行完成并收集结果，超时自动终止未完成的任务并退出
 *
 * @author weijiayu
 * @date 2025/3/14 10:48
 */
@Slf4j
public class ParallelCallableUtil {

    // 任务保底最短超时时间
    private static final Long MIN_TASK_TIME_OUT_MS = 100L;

    /**
     * 并行执行任务列表，并收集返回结果
     *
     * @param taskTimeOutSecond 整体任务超时时间，不是单个
     * @param callableList      任务列表
     * @return java.util.HashMap<java.util.concurrent.Callable, java.lang.Object> 任务执行结果集合
     * @date 2025/3/14 11:13
     */
    public static HashMap<Callable, Object> runAndWaitForComplete(Integer taskTimeOutSecond,
                                                                  List<Callable> callableList) {
        HashMap<Callable, Object> resultMap = new HashMap<>();
        HashMap<Future, Callable> futureCallableMap = new LinkedHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(callableList.size());
        for (Callable callable : callableList) {
            futureCallableMap.put(executorService.submit(callable), callable);
        }
        executorService.shutdown();
        try {
            // 遍历任务，收集结果
            Long remainTimeMs = taskTimeOutSecond * 1000L;
            for (Map.Entry<Future, Callable> entry : futureCallableMap.entrySet()) {
                // 计算本次任务耗时，并刷新总耗时。保证所有任务在期望的指定时间内完成（超时则自动终止退出）
                Long startTime = System.currentTimeMillis();
                // 阻塞等待结果
                // 设置最小超时时间，避免任务列表中有长时长的任务占用大部分时间导致其他任务因超时而取不到结果（实际已经完成）
                Object result = entry.getKey().get(Math.max(remainTimeMs, MIN_TASK_TIME_OUT_MS), TimeUnit.MILLISECONDS);
                Long costTime = System.currentTimeMillis() - startTime;
                remainTimeMs -= costTime;
                resultMap.put(entry.getValue(), result);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        } finally {
            // 取消未完成任务
            for (Future future : futureCallableMap.keySet()) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
        }
        return resultMap;
    }
}
