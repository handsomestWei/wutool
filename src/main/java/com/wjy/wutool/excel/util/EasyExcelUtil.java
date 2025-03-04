package com.wjy.wutool.excel.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.wjy.wutool.excel.ImageAutoFillMergeCelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

/**
 * 阿里EasyExcel工具类
 *
 * @author weijiayu
 * @date 2023/4/1 10:55
 */
@Slf4j
public class EasyExcelUtil {

    /**
     * http excel附件下载
     */
    public static void rspAttachment(HttpServletResponse response, String fileName, String fileSuffix,
                                     String sheetName, Class dataClazz, Collection<?> data) {
        try {
            response.flushBuffer();
            ExcelWriterBuilder builder = EasyExcel.write(response.getOutputStream(), dataClazz);
            setAttachment(response, builder, fileName, fileSuffix);
            setAutoFormat(builder);
            try (ExcelWriter excelWriter = builder.build()) {
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
                excelWriter.write(data, writeSheet);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * http excel附件下载，基于excel模板填充内容
     */
    public static void rspAttachmentWithTpl(HttpServletResponse response, String tplPath, String fileName, Object data) {
        try {
            ExcelWriterBuilder builder = EasyExcel.write(response.getOutputStream());
            setAttachment(response, builder, fileName, tplPath.substring(tplPath.lastIndexOf(".")));
            builder.withTemplate(tplPath).registerWriteHandler(new ImageAutoFillMergeCelHandler()).sheet().doFill(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * http excel附件下载设置
     */
    public static ExcelWriterBuilder setAttachment(HttpServletResponse response, ExcelWriterBuilder builder,
                                                   String fileName, String fileSuffix) throws UnsupportedEncodingException {
        /**
         * 需要显式设置类型，否则会报工作薄类型错误org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException: The supplied data
         * appears to be in the OLE2 Format. You are calling the part of POI that deals with OOXML (Office Open XML)
         * Documents. You need to call a different part of POI to process this data (eg HSSF instead of XSSF)
         */
        switch (fileSuffix) {
            case ".xlsx":
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                builder = builder.excelType(ExcelTypeEnum.XLSX);
                break;
            case ".xls":
                response.setContentType("application/vnd.ms-excel");
                builder = builder.excelType(ExcelTypeEnum.XLS);
                break;
            default:
                // TODO
                break;
        }
        // 避免文件名中文乱码
        fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        // 设置响应头附件
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + fileSuffix);
        response.setCharacterEncoding("UTF-8");
        return builder;
    }

    /**
     * 设置自适应列宽
     */
    public static ExcelWriterBuilder setAutoFormat(ExcelWriterBuilder builder) {
        return builder.useDefaultStyle(false).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());
    }
}
