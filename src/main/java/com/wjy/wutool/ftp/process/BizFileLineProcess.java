package com.wjy.wutool.ftp.process;

/**
 * 文件内容处理接口
 * 
 * @author weijiayu
 * @date 2023/11/3 15:22
 */
public interface BizFileLineProcess {

    /**
     * 处理单行
     * 
     * @author weijiayu
     * @date 2023/11/3 15:23
     * @param line
     * @return java.lang.Boolean
     * @throws Exception
     */
    Boolean process(String line) throws Exception;
}
