package com.wjy.wutool.ftp.filter;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.springframework.stereotype.Component;

/**
 * 文件过滤器，只下载csv文件。在url中指定filter=#ftpCsvFilter
 * 
 * @author weijiayu
 * @date 2023/11/3 10:43
 */
@Component
public class FtpCsvFilter implements GenericFileFilter<Object> {

    @Override
    public boolean accept(GenericFile<Object> file) {
        // 遍历文件夹，对文件夹直接放行
        return file.getFileName().endsWith(".csv") || file.isDirectory();
    }
}
