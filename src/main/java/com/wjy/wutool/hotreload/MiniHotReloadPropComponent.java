package com.wjy.wutool.hotreload;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.setting.yaml.YamlUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 配置文件轻量热更新。基于最小化原则，适用于少量配置的修改
 *
 * @author weijiayu
 * @date 2025/02/20 23:36
 */
@Slf4j
@Component
@Configuration
@ConfigurationProperties("wutool.hotreload.prop")
@ConditionalOnProperty(name = "wutool.hotreload.prop.monitor-file")
public class MiniHotReloadPropComponent extends FileAlterationListenerAdaptor implements InitializingBean {

    @Getter
    @Setter
    private String monitorFile; // 配置文件监控路径。只支持单文件

    @Getter
    @Setter
    private Long monitorIntervalSec = 5L; // 配置文件更新监控间隔，单位秒

    /**
     * 热更新支持的属性列表，逗号分隔
     * 存在配置文件定义的属性名称，和bean内注入绑定的属性名称不一致的情况，因此支持以下两种格式：
     * 支持格式1：<配置文件定义的属性名称>#<spring bean名称>#<bean内对应的字段名称>
     * 支持格式2：<配置文件定义的属性名称>#<spring bean名称>
     */
    @Getter
    @Setter
    private List<String> paramList = new ArrayList<>();

    private HashSet<String> supportPropFileSuffixSet = new HashSet<>(Arrays.asList("yml", "yaml", "properties"));
    private FileAlterationMonitor fileMonitor;
    private HashMap<String, PropParam> supportPropParamMap = new HashMap();


    @Override
    public void onFileChange(File file) {
        log.debug("hot reload prop file change, file={}", file.getAbsolutePath());
        HashMap<String, Object> propMap = loadPropFile(file);
        if (propMap == null || propMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, PropParam> propParamEntry : supportPropParamMap.entrySet()) {
            String propName = propParamEntry.getKey();
            if (propMap.containsKey(propName)) {
                PropParam propParam = propParamEntry.getValue();
                String beanName = propParam.beanName;
                Object propVal = propMap.get(propName);
                this.setPropVal(beanName, propName, propVal);
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        this.supportPropParamMap = this.parsePropParamList(this.paramList);
        this.fileMonitor = this.initFileMonitor(this.monitorFile, this.monitorIntervalSec);
    }

    public Boolean setPropVal(String beanName, String propName, Object propVal) {
        try {
            if (!this.supportPropParamMap.containsKey(propName)) {
                return false;
            }
            Object targetBean = SpringUtil.getBean(beanName);
            if (targetBean == null) {
                return false;
            }
            String fieldName = this.supportPropParamMap.get(propName).fieldName;
            BeanUtil.setFieldValue(targetBean, fieldName, propVal);
            log.debug("hot reload prop has set, propName={}, propVal={}", propName, propVal);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public void destroy() {
        try {
            if (fileMonitor != null) {
                fileMonitor.stop();
                log.info("hot reload monitor prop file end, file={}", this.monitorFile);
            }
        } catch (Exception ignored) {
        }
    }

    private HashMap<String, Object> loadPropFile(File file) {
        HashMap<String, Object> propMap = new HashMap<>();
        try {
            // 根据配置文件名后缀，自动识别格式
            String fileNameSuffix = FilenameUtils.getExtension(file.getName());
            switch (fileNameSuffix) {
                case "yml":
                case "yaml":
                    // TODO key按yml格式的层级做拼接
                    propMap = YamlUtil.loadByPath(file.getAbsolutePath());
                    break;
                case "properties":
                    Properties prop = new Properties();
                    prop.load(Files.newInputStream(file.toPath()));
                    for (Object propName : prop.keySet()) {
                        propMap.put(propName.toString(), prop.get(prop.get(propName)));
                    }
                    break;
                case "json":
                    // TODO
                    break;
                default:
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return propMap;
    }

    // 配置项解析
    private HashMap<String, PropParam> parsePropParamList(List<String> propParamList) {
        HashMap<String, PropParam> propParamMap = new HashMap<>();
        if (CollectionUtils.isEmpty(propParamList)) {
            return propParamMap;
        }
        for (String props : propParamList) {
            PropParam propParam = new PropParam();
            String[] paramArray = props.split("#");
            if (paramArray.length <= 1) {
                continue;
            }
            propParam.propName = paramArray[0];
            propParam.beanName = paramArray[1];
            propParam.fieldName = propParam.propName;
            if (paramArray.length > 2) {
                propParam.fieldName = paramArray[2];
            }
            propParamMap.put(propParam.propName, propParam);
        }
        return propParamMap;
    }

    private FileAlterationMonitor initFileMonitor(String propFilePath, Long intervalSec) {
        try {
            if (!this.verifyPropFile(propFilePath)) {
                log.info("hot reload monitor prop file not support, file={}", propFilePath);
                return null;
            }
            File f = new File(propFilePath);
            String monitorFileDir = f.getParent();
            String monitorFileName = f.getName();
            FileAlterationObserver observer = new FileAlterationObserver(monitorFileDir,
                    (subFile) -> subFile.getName().equals(monitorFileName));
            observer.addListener(this);
            FileAlterationMonitor monitor = new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(intervalSec), observer);
            monitor.start();
            log.info("hot reload monitor prop file start, file={}", propFilePath);
            return monitor;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private Boolean verifyPropFile(String propFilePath) {
        try {
            if (StringUtils.isEmpty(propFilePath)) {
                return false;
            }
            File propFile = new File(propFilePath);
            if (!propFile.exists() || propFile.isDirectory()) {
                return false;
            }
            String fileNameSuffix = FilenameUtils.getExtension(propFile.getName());
            return this.supportPropFileSuffixSet.contains(fileNameSuffix);
        } catch (Exception ignored) {
            return false;
        }
    }

    private class PropParam {

        private String propName;
        private String beanName;
        private String fieldName;
    }
}