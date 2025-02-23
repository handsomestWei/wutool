package com.wjy.wutool.util;

import java.lang.ref.SoftReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 内存定时kv缓存。可避免引入额外中间件
 * 
 * @author weijiayu
 * @date 2023/8/24 16:23
 */
public class MemoryTimedKvCacheUtil {

    private static ConcurrentMap<String, SoftReference<Object>> cacheMap = new ConcurrentHashMap<>();

    /**
     * 缓存kv，单位秒
     * 
     * @author weijiayu
     * @date 2023/8/24 16:27
     * @param key
     * @param value
     * @param second
     * @return void
     */
    public static void cacheSecond(String key, Object value, int second) {
        SoftReference<Object> sf = new SoftReference<Object>(value);
        cacheMap.put(key, sf);
        Timer timer = new Timer();
        timer.schedule(new DeleteTask(key, timer), second * 1000);
    }

    public static Object getValue(String key) { return cacheMap.get(key).get(); }

    public static Boolean containsKey(String key) {
        return cacheMap.containsKey(key);
    }

    private static class DeleteTask extends TimerTask {
        private String key;
        private Timer timer;

        public DeleteTask(String key, Timer timer) {
            this.key = key;
            this.timer = timer;
        }

        @Override
        public void run() {
            try {
                cacheMap.remove(key);
                timer.cancel();
                if (null != timer) {
                    timer = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
