package com.wjy.wutool.ftp.route;

import com.wjy.wutool.ftp.process.BizBizFileProcess;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Apache Camel FTP路由配置，分别下载csv文件和jpg图片
 * 启用配置：camel.springboot.main-run-controller=true
 *
 * @link https://cloud.tencent.com/developer/article/1707868
 * @link https://camel.apache.org/components/4.0.x/ftp-component.html
 * @author weijiayu
 * @date 2023/11/3 10:32
 */
@Component
@Slf4j
@Configuration
@ConfigurationProperties("wutool.ftp")
@ConditionalOnProperty(name = "camel.springboot.main-run-controller")
public class BizFtpRouteBuilder extends RouteBuilder {

    /**
     * 配置例：ftp://xxx.xxx.xxx.xxx:21?username=wiseftp&password=wiseftp&filter=#ftpCsvFilter&recursive=true&reconnectDelay=1000&binary=true&passiveMode=true&delete=true&delay=500&noop=true&idempotent=true&ftpClient.controlEncoding=GBK&readLock=rename
     */
    @Getter
    @Setter
    private String fileServerUrl;

    /**
     * 配置例：file:F:\\fff
     */
    @Getter
    @Setter
    private String fileLocalDir;

    /**
     * 配置例：ftp://xxx.xxx.xxx.xxx:21?username=wiseftp&password=wiseftp&filter=#ftpImgFilter&recursive=true&reconnectDelay=1000&binary=true&passiveMode=true&delete=true&delay=500&noop=true&idempotent=true&ftpClient.controlEncoding=GBK&readLock=rename
     */
    @Getter
    @Setter
    private String imgServerUrl;

    /**
     * 配置例：file:F:\\fff
     */
    @Getter
    @Setter
    private String imgLocalDir;

    @Autowired(required = false)
    private BizBizFileProcess bizFileProcess;

    @Override
    public void configure() throws Exception {
        // 下载csv文件
        from(fileServerUrl).to(fileLocalDir).process(bizFileProcess).log(LoggingLevel.DEBUG, log,
            "Download biz file ${file:name} complete.");
        // 下载jpg图片
        from(imgServerUrl).to(imgLocalDir).log(LoggingLevel.DEBUG, log,
            "Download biz img ${file:name} complete.");
    }
}
