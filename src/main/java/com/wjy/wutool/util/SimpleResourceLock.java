package com.wjy.wutool.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * 简单资源操作锁。定义资源操作对象T key
 *
 * @author weijiayu
 * @date 2025/3/18 0:06
 */
@Slf4j
public class SimpleResourceLock<T> {

    private ConcurrentHashMap<T, ReentrantLock> rsLockMap;

    public SimpleResourceLock() {
        this.rsLockMap = new ConcurrentHashMap<>();
    }

    public Object doApply(T key, Function<T, Object> applyFunc) {
        if (key == null) {
            return null;
        }
        ReentrantLock rslLock = rsLockMap.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            rslLock.lock();
            return applyFunc.apply(key);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            rslLock.unlock();
        }
    }
}
