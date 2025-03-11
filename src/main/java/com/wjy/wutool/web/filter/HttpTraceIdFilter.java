package com.wjy.wutool.web.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * http调用链id注入过滤器。在http请求和响应头中，注入调用链traceId。没有加spanId，只适合单体应用使用</br>
 * 继承了spring的OncePerRequestFilter类，保证该过滤器在链中优先执行</br>
 * 使用方式：在前端通过浏览器等查看响应头可以获取到该id，配合后端logback.xml配置增加格式化输出%X{traceId}，能在日志中完整追踪请求的过程。</br>
 *
 * @author weijiayu
 * @date 2024/11/25 9:25
 */
@Component
public class HttpTraceIdFilter extends OncePerRequestFilter {

    private static final String APP_TRACE_ID = "X-App-Trace-Id";
    private static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 优先从请求头获取
            String traceId = request.getHeader(APP_TRACE_ID);
            if (StringUtils.isEmpty(traceId)) {
                traceId = UUID.randomUUID().toString().replaceAll("-", "");
            }
            MDC.put(TRACE_ID, traceId);
            // 回填响应头
            response.addHeader(TRACE_ID, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
