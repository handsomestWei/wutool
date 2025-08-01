package com.wjy.wutool.web.i18n;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * 国际化语言拦截器。优先从请求头中获取语言，如果获取不到，则使用默认语言。
 */
@Component
@Slf4j
public class LanguageInterceptor implements HandlerInterceptor {

    public static final String HEADER_LANGUAGE = "language"; // 请求头中语言字段

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Locale locale = Locale.getDefault();
        try {
            String language = request.getHeader(HEADER_LANGUAGE);
            if (StringUtils.isNotEmpty(language)) {
                // eg: zh-CN
                locale = new Locale(language.split("-")[0], language.split("-")[1]);
            }
        } catch (Exception ignore) {
        }
        // 放入当前请求线程上下文中，MessageUtil.getMessageWithContext方法会从LocaleContextHolder获取设置的国际化信息
        LocaleContextHolder.setLocale(locale);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
    }

}
