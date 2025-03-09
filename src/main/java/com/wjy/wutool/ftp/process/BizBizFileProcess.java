package com.wjy.wutool.ftp.process;

import com.wjy.wutool.ftp.route.BizFtpRouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFileMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.RandomAccessFile;

/**
 * 文件ftp工作流处理器
 * 
 * @author weijiayu
 * @date 2023/11/3 10:47
 */
@Component
public class BizBizFileProcess extends AbsBizFileProcess implements Processor {

    @Autowired(required = false)
    private BizFtpRouteBuilder bizFtpRouteBuilder;

    // 自定义文件处理接口实现类
    @Resource
    private BizFileLineProcess ctmBizFileService;

    @Override
    public String getCheckPointFileName() {
        return bizFtpRouteBuilder.getFileLocalDir() + "biz-checkPoint";
    }

    @Override
    public BizFileLineProcess getBizProcess() {
        return ctmBizFileService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        // ftp下载文件完成，开始处理
        GenericFileMessage<RandomAccessFile> inMsg = (GenericFileMessage<RandomAccessFile>)exchange.getIn();
        String fileName = inMsg.getGenericFile().getFileName();
        process(bizFtpRouteBuilder.getFileLocalDir() + fileName);
    }
}
