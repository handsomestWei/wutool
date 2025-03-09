package com.wjy.wutool.ftp.process;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于Apache Camel FTP的文件下载处理模板类。支持并发文件处理，支持处理进度持久化和重加载（适用于长耗时的大文件）
 *
 * @see com.wjy.wutool.ftp.route.BizFtpRouteBuilder ftp下载配置参考
 *
 * @author weijiayu
 * @date 2023/11/3 11:50
 */
@Slf4j
public abstract class AbsBizFileProcess implements InitializingBean, DisposableBean {

    // 线程关闭信号
    private volatile Boolean stopSignal = false;
    // 文件处理进度，key=文件名，value=处理到的行号
    private volatile ConcurrentHashMap<String, Long> checkPointMap = new ConcurrentHashMap<>();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(30, 50, 2, TimeUnit.MINUTES,
        new LinkedBlockingQueue<>(1000), new ThreadPoolExecutor.AbortPolicy());

    public abstract String getCheckPointFileName();

    public abstract BizFileLineProcess getBizProcess();

    public void process(String fileName) {
        // 每个线程处理一个文件
        threadPoolExecutor.execute(() -> {
            File f = new File(fileName);
            if (!f.exists() || f.isDirectory()) {
                clearCheckPoint(fileName);
                return;
            }
            Long startNo = 0L;
            if (checkPointMap.contains(fileName)) {
                startNo = checkPointMap.get(fileName);
            }
            Long procNo = 0L;
            // 遍历每行
            try (LineIterator lineIter = FileUtils.lineIterator(f)) {
                if (stopSignal) {
                    setCheckPoint(fileName, procNo);
                    return;
                }
                while (lineIter.hasNext()) {
                    if (procNo < startNo) {
                        // 跳过已处理行号
                        procNo++;
                        continue;
                    }
                    // 业务处理，逐行
                    getBizProcess().process(lineIter.next());
                    procNo++;
                    setCheckPoint(fileName, procNo);
                }
                clearCheckPoint(fileName);
                FileUtils.forceDelete(f);
            } catch (Exception e) {
                log.error(fileName + " process line " + procNo + " error", e);
                setCheckPoint(fileName, procNo);
            }
        });
    }

    // 加载未处理完的进度
    private void loadChekPoint() throws IOException {
        File checkPointFile = new File(getCheckPointFileName());
        if (!checkPointFile.exists()) {
            return;
        }
        try (LineIterator lineIter = FileUtils.lineIterator(checkPointFile)) {
            while (lineIter.hasNext()) {
                String[] proc = lineIter.nextLine().split("=");
                checkPointMap.put(proc[0], Long.valueOf(proc[1]));
                // 继续上次处理
                process(proc[0]);
            }
        }
        // 加载后删除
        FileUtils.forceDelete(checkPointFile);
    }

    // 将处理中的文件和行号落盘写文件保存
    private void saveCheckPoint() throws IOException {
        if (CollectionUtils.isNotEmpty((Collection<?>)checkPointMap)) {
            List<String> procLs = new LinkedList<>();
            for (Map.Entry<String, Long> entry : checkPointMap.entrySet()) {
                procLs.add(entry.getKey() + "=" + entry.getValue());
            }
            File checkPointFile = new File(getCheckPointFileName());
            checkPointFile.createNewFile();
            FileUtils.writeLines(checkPointFile, procLs, true);
        }
    }

    private void setCheckPoint(String fileName, Long lineNo) {
        if (StringUtils.isNotEmpty(fileName)) {
            checkPointMap.put(fileName, lineNo);
        }
    }

    private void clearCheckPoint(String fileName) {
        if (checkPointMap.contains(fileName)) {
            checkPointMap.remove(fileName);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 启动时，尝试加载上次未处理完的文件名和处理到的行号信息到内存
        loadChekPoint();
    }

    @Override
    public void destroy() throws Exception {
        // jvm退出钩子，通知所有线程停止
        stopSignal = true;
        // threadPoolExecutor.shutdownNow();
        threadPoolExecutor.shutdown();
        saveCheckPoint();
    }
}
