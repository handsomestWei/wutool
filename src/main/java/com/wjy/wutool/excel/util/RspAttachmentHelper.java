package com.wjy.wutool.excel.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.wjy.wutool.excel.ImageAutoFillMergeCelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * excel http附件下载
 *
 * @author weijiayu
 * @date 2025/3/5 0:42
 */
@Slf4j
public class RspAttachmentHelper {

    /**
     * excel http附件下载
     */
    public static void downLoad(HttpServletResponse response, String fileName, String fileSuffix,
                                     String sheetName, Collection<?> data) {
        try {
            ExcelWriterBuilder builder = EasyExcel.write(response.getOutputStream());
            EasyExcelUtil.setAttachment(response, builder, fileName, fileSuffix);
            try (ExcelWriter excelWriter = builder.build()) {
                EasyExcelUtil.writerSheet(excelWriter, sheetName, data);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * excel http附件下载，基于excel模板填充内容
     */
    public static void downLoadWithTpl(HttpServletResponse response, String tplPath, String fileName, Object data) {
        try {
            ExcelWriterBuilder builder = EasyExcel.write(response.getOutputStream());
            EasyExcelUtil.setAttachment(response, builder, fileName, tplPath.substring(tplPath.lastIndexOf(".")));
            builder.withTemplate(tplPath).registerWriteHandler(new ImageAutoFillMergeCelHandler()).sheet().doFill(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
