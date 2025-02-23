package com.wjy.wutool.util;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于时间间隔的内存kv缓存过滤器。可用于流量削峰
 *
 * @author weijiayu
 * @date 2024/7/19 11:17
 */
public class MemoryKvCacheDurationFilter {

    private Long cap = 0L;
    private ChronoUnit chronoUnit;
    private Long duration = 0L;
    private ConcurrentHashMap<String, Date> kvTimeMap;
    private ScheduledExecutorService scheduler;

    /**
     *
     * @param cap 过滤器的缓存容量
     * @param chronoUnit 缓存的时间单位
     * @param duration 缓存的持续时间
     */
    public MemoryKvCacheDurationFilter(Long cap, ChronoUnit chronoUnit, Long duration) {
        this.cap = cap;
        this.chronoUnit = chronoUnit;
        this.duration = duration;
        kvTimeMap = new ConcurrentHashMap<String, Date>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new InnerCleanThread(chronoUnit, duration), 0, 5, TimeUnit.MINUTES);
    }

    public MemoryKvCacheDurationFilter(Long cap, ChronoUnit chronoUnit, Long duration, long period, TimeUnit timeUnit) {
        this.cap = cap;
        this.chronoUnit = chronoUnit;
        this.duration = duration;
        kvTimeMap = new ConcurrentHashMap<String, Date>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new InnerCleanThread(chronoUnit, duration), 0, period, timeUnit);
    }

    public Boolean checkAndCacheKvIsOutOfDuration(String key, Date date) {
        if (kvTimeMap.size() >= cap || key == null || date == null) {
            // 容量满了
            return true;
        }
        Date oldDate = kvTimeMap.get(key);
        if (oldDate == null) {
            // 第一次
            kvTimeMap.put(key, date);
            return true;
        }
        if (chronoUnit.between(oldDate.toInstant(), date.toInstant()) > duration) {
            // 超时，原来的失效，缓存新的
            kvTimeMap.put(key, date);
            return true;
        } else {
            return false;
        }
    }

    private class InnerCleanThread implements Runnable {

        private ChronoUnit chronoUnit;
        private Long duration;

        public InnerCleanThread(ChronoUnit chronoUnit, Long duration) {
            this.chronoUnit = chronoUnit;
            this.duration = duration;
        }

        @Override
        public void run() {
            try {
                // 定时清除超时key
                kvTimeMap.entrySet().removeIf(
                    (entry) -> (chronoUnit.between(entry.getValue().toInstant(), new Date().toInstant()) > duration));
            } catch (Exception e) {
                return;
            }
        }
    }

}
