package com.wjy.wutool.msg.reactor.service;

/**
 * 简单消息响应模型。将三方的异步响应，包装成阻塞等待（超时丢弃）同步响应
 * 其中泛型Y为消息请求，Z为消息响应。消息请求id保证唯一，响应消息的id和请求时一致
 * 
 * @author weijiayu
 * @date 2024/4/29 17:25
 */

import com.alibaba.fastjson.JSON;
import com.wjy.wutool.msg.reactor.dto.AbsBaseMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbsMsgReactorService<Y extends AbsBaseMsg, Z extends AbsBaseMsg> {

    /** 消息响应缓存，key为消息请求id */
    private HashMap<Long, Z> msgResultMap = new HashMap<>();
    /** 对key加锁 */
    private ConcurrentHashMap<Long, Object> msgKeyLockMap = new ConcurrentHashMap<>();
    /** 等待消息响应阻塞时间，单位秒 */
    public Integer msgWaitSec = 2;

    /**
     * 消息发送
     * 
     * @author weijiayu
     * @date 2024/4/30 11:53
     * @param msg
     * @return java.lang.Boolean
     */
    public abstract Boolean sendMsg(String msg);

    /**
     * 响应消息解析。交给实现者处理，避免泛型擦除
     * 
     * @author weijiayu
     * @date 2024/5/9 11:17
     * @param msg
     * @return Z
     */
    public abstract Z parseRspMsg(String msg);

    /**
     * 消息处理。消息接收后用
     * 
     * @author weijiayu
     * @date 2024/4/30 11:54
     * @param msgObj
     * @return java.lang.Boolean
     */
    public abstract Boolean handleMsgWithBiz(Z msgObj);

    /**
     * 发送消息并获取响应结果
     * 
     * @author weijiayu
     * @date 2024/4/30 11:55
     * @param msgObj
     * @return Z
     */
    public Z sendMsgAndGetResult(Y msgObj) {
        Long msgId = msgObj.getMsgId();
        boolean sendResult = sendMsg(JSON.toJSONString(msgObj));
        if (!sendResult) {
            return null;
        }
        // 发送成功，阻塞等待结果
        try {
            doInitResource(msgId);
            // TODO 硬休眠等待，可以改成类似future task get方式while循环并判断起始时间是否超时，提升响应速度
            TimeUnit.SECONDS.sleep(msgWaitSec);
            return msgResultMap.get(msgId);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return null;
        } finally {
            // 擦除记录。取后删除，或等待超时丢弃
            doReleaseResource(msgId);
        }
    }

    /**
     * 消息处理模板方法，子类可以直接使用
     */
    public Boolean handleMsg(String msg) {
        try {
            Z msgObj = parseRspMsg(msg);
            doPutResource(msgObj);
            return handleMsgWithBiz(msgObj);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return false;
        }
    }

    /**
     * 申请资源
     */
    private void doInitResource(Long msgId) {
        msgKeyLockMap.put(msgId, new Object());
        msgResultMap.put(msgId, null);
    }

    /**
     * 释放资源
     */
    private void doReleaseResource(Long msgId) {
        Object resource = msgKeyLockMap.get(msgId);
        if (resource == null) {
            return;
        }
        synchronized (resource) {
            msgKeyLockMap.remove(msgId);
            msgResultMap.remove(msgId);
        }
    }

    /**
     * 放置资源
     */
    private Boolean doPutResource(Z msgObj) {
        Long msgId = msgObj.getMsgId();
        if (!msgKeyLockMap.containsKey(msgId)) {
            return false;
        }
        Object resource = msgKeyLockMap.get(msgId);
        if (resource == null) {
            return false;
        }
        // 对key加锁。避免在删除后执行put操作，对象永远不得释放，占用内存
        synchronized (resource) {
            // 指令执行顺序有先后，重入后第二次判断
            if (!msgKeyLockMap.containsKey(msgId)) {
                return false;
            }
            msgResultMap.put(msgId, msgObj);
            return true;
        }
    }
}
