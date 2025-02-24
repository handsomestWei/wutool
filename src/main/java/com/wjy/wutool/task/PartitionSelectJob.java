package com.wjy.wutool.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * 针对大in查询，做拆分和合并。支持线程加速，需要在合并函数自行处理线程冲突
 * 
 * @author weijiayu
 * @date 2024/8/26 9:57
 */
@Slf4j
public class PartitionSelectJob<J, K> {

    /** in条件查询 */
    private SelectInFunction<J> selectInFunc;
    /** 查询结果合并 */
    private BiConsumer<List<J>, ConcurrentHashMap<String, K>> combineFunc;
    private ConcurrentHashMap<String, K> rsDataMap = new ConcurrentHashMap<>();

    private Integer maxInValue = 1000;
    private Integer poolSize = 1;
    private Integer poolWaitSec = 2;

    public PartitionSelectJob(SelectInFunction<J> selectInFunc,
                              BiConsumer<List<J>, ConcurrentHashMap<String, K>> combineFunc) {
        this.selectInFunc = selectInFunc;
        this.combineFunc = combineFunc;
    }

    public PartitionSelectJob(SelectInFunction<J> selectInFunc,
                              BiConsumer<List<J>, ConcurrentHashMap<String, K>> combineFunc, Integer maxInValue, Integer poolSize,
                              Integer poolWaitSec) {
        this.selectInFunc = selectInFunc;
        this.combineFunc = combineFunc;
        this.maxInValue = maxInValue;
        this.poolSize = poolSize;
        this.poolWaitSec = poolWaitSec;
    }

    public List<K> execute(List<String> inDataList, Object... sqlParams) {
        List<K> rs = new ArrayList<>();
        try {
            if (CollectionUtils.isEmpty(inDataList)) {
                return rs;
            }
            ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
            // 对in条件数据拆分
            for (List<String> subInDataList : ListUtils.partition(inDataList, maxInValue)) {
                executorService.submit(() -> {
                    try {
                        // 将一次大in数据查询，拆分为多线程执行
                        List<J> subRs = multipleApply(selectInFunc, subInDataList, sqlParams);
                        // 合并查询结果
                        combineFunc.accept(subRs, rsDataMap);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
            executorService.shutdown();
            // 最大超时等待2秒
            executorService.awaitTermination(poolWaitSec, TimeUnit.SECONDS);
            rs.addAll(rsDataMap.values());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
        return rs;
    }

    public List<J> multipleApplyExt(SelectInFunction<J> selectInFunc, List<String> inDataList, Object... sqlParams) {
        return new ArrayList<>();
    }

    // TODO 对可变长参数，暂时根据长度手动拼接
    private List<J> multipleApply(SelectInFunction<J> selectInFunc, List<String> inDataList, Object... sqlParams) {
        if (sqlParams == null) {
            return selectInFunc.apply(inDataList);
        }
        int len = sqlParams.length;
        if (len == 0) {
            return selectInFunc.apply(inDataList);
        } else if (len == 1) {
            return selectInFunc.apply(inDataList, sqlParams[0]);
        } else if (len == 2) {
            return selectInFunc.apply(inDataList, sqlParams[0], sqlParams[1]);
        } else if (len == 3) {
            return selectInFunc.apply(inDataList, sqlParams[0], sqlParams[1], sqlParams[2]);
        } else {
            // 可以继承父类复写该方法做扩展
            return multipleApplyExt(selectInFunc, inDataList, sqlParams);
        }
    }

    /**
     * 针对select in条件查询，自定义lambada函数。大in要放在入参首位，使用可变长参数支持复杂sql传参
     * 
     * @author weijiayu
     * @date 2024/8/26 16:10
     */
    @FunctionalInterface
    public interface SelectInFunction<T> {

        /**
         * TODO 使用可变长参数，如果方法有多态，可能会导致执行非期望方法。暂时只能修改方法签名
         *
         * @author weijiayu
         * @date 2024/8/26 11:35
         * @param inDataList
         * @param sqlParams
         * @return java.util.List<T>
         */
        List<T> apply(List<String> inDataList, Object... sqlParams);
    }
}
