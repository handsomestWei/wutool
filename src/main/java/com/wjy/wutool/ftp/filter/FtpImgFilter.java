package com.wjy.wutool.ftp.filter;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.springframework.stereotype.Component;

/**
 * 
 * 图片过滤器，只下载jpg文件。在url中指定filter=#ftpImgFilter
 * 
 * @author weijiayu
 * @date 2023/11/3 10:40
 */
@Component
public class FtpImgFilter implements GenericFileFilter<Object> {

    @Override
    public boolean accept(GenericFile<Object> file) {
        // 遍历文件夹，对文件夹直接放行
        return file.getFileName().endsWith(".jpg") || file.isDirectory();
    }
}
