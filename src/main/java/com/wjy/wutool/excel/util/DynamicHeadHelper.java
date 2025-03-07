package com.wjy.wutool.excel.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import org.apache.poi.ss.usermodel.Row;

import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;

/**
 * excel动态列写入
 *
 * @author weijiayu
 * @date 2025/3/5 0:19
 */
public class DynamicHeadHelper {

    /**
     * excel动态列写入
     *
     * @param outputStream
     * @param sheetName
     * @param headNameAliaMap 标题头。按link顺序，value为标题名称
     * @param columnApplyMap  列转换函数集合，支持自定义apply函数对该列数据做转换
     * @param dataList
     * @return void
     * @date 2024/11/18 15:27
     * @link https://easyexcel.opensource.alibaba.com/docs/current/quickstart/write#%E5%8A%A8%E6%80%81%E5%A4%B4%E5%AE
     * %9E%E6%97%B6%E7%94%9F%E6%88%90%E5%A4%B4%E5%86%99%E5%85%A5
     */
    public static void dynamicHeadWrite(OutputStream outputStream, String sheetName,
                                        LinkedHashMap<String, String> headNameAliaMap,
                                        HashMap<String, Function> columnApplyMap, List<Map<String, Object>> dataList) {
        ExcelWriterBuilder builder = EasyExcel.write(outputStream);
        setExcelExcelHead(builder, headNameAliaMap);
        try (ExcelWriter excelWriter = builder.build()) {
            EasyExcelUtil.writerSheet(excelWriter, sheetName, convertRowData(headNameAliaMap, columnApplyMap, dataList));
        }
    }

    /**
     * 为excel设置行头
     * @param builder
     * @param headNameAliaMap 标题头。按link顺序，value为标题名称
     * @return
     */
    public static ExcelWriterBuilder setExcelExcelHead(ExcelWriterBuilder builder,
                                                       LinkedHashMap<String, String> headNameAliaMap) {
        return builder.head(genExcelHeads(headNameAliaMap));
    }

    /**
     * 生成Excel行头
     *
     * @param headNameAliaMap 标题头。按link顺序，value为标题名称
     * @return java.util.List<java.util.List < java.lang.String>>
     * @link https://easyexcel.opensource.alibaba.com/docs/current/quickstart/write#%E5%8A%A8%E6%80%81%E5%A4%B4%E5%AE
     * %9E%E6%97%B6%E7%94%9F%E6%88%90%E5%A4%B4%E5%86%99%E5%85%A5
     * @date 2024/11/18 16:44
     */
    public static List<List<String>> genExcelHeads(LinkedHashMap<String, String> headNameAliaMap) {
        List<List<String>> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : headNameAliaMap.entrySet()) {
            List<String> headList = new ArrayList<>();
            String headName = entry.getKey();
            String headAliaName = entry.getValue();
            // deal com.alibaba.excel.exception.ExcelGenerateException: head name can not be null.
            headList.add(headAliaName != null ? headAliaName : headName);
            list.add(headList);
        }
        return list;
    }

    /**
     * excel行数据转换
     *
     * @param headNameAliaMap 标题头。按link顺序，key为字段名
     * @param columnApplyMap  列转换函数集合，支持自定义apply函数对该列数据做转换
     * @param dataList
     * @return java.util.List<java.util.Map < java.lang.Integer, java.lang.Object>>
     * @date 2024/11/18 16:18
     * @see com.alibaba.excel.write.executor.ExcelWriteAddExecutor#doAddBasicTypeToExcel(RowData, Head, Row, int, int, int, int)
     * @see com.alibaba.excel.write.metadata.MapRowData#get(int)
     */
    public static List<Map<Integer, Object>> convertRowData(LinkedHashMap<String, String> headNameAliaMap,
                                                             HashMap<String, Function> columnApplyMap,
                                                             List<Map<String, Object>> dataList) {
        List<Map<Integer, Object>> rowDataList = new ArrayList<>();
        for (Map<String, Object> dataMap : dataList) {
            Map<Integer, Object> rowDataMap = new HashMap<>();
            // @link https://easyexcel.alibaba.com/expert/question-history-15935#%E5%88%86%E6%9E%90%E9%97%AE%E9%A2%98%E5%8E%9F%E5%9B%A0
            int i = 0;
            for (String headName : headNameAliaMap.keySet()) {
                Object val = dataMap.get(headName);
                if (columnApplyMap != null && columnApplyMap.get(headName) != null) {
                    val = columnApplyMap.get(headName).apply(val);
                }
                rowDataMap.put(i, val);
                i++;
            }
            rowDataList.add(rowDataMap);
        }
        return rowDataList;
    }
}
