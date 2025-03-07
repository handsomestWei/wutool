package com.wjy.wutool.excel.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;

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

    public static void writerSheet(ExcelWriter excelWriter, String sheetName, Collection<?> data) {
        WriteSheet writeSheet = setAutoFormat(EasyExcel.writerSheet(sheetName)).build();
        excelWriter.write(data, writeSheet);
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
    public static ExcelWriterSheetBuilder setAutoFormat(ExcelWriterSheetBuilder sheetBuilder) {
        return sheetBuilder.useDefaultStyle(false).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());
    }
}
