package com.wjy.wutool.web.i18n;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * i18n国际化工具类，规避某些场景下因缓存取不到期望值问题
 *
 * @author weijiayu
 * @date 2025/3/18 23:55
 */
@Slf4j
public class MessageUtil {

    // 配置文件名，注意格式和对应语言的资源文件的命名保持一致，例：语言_国家，zh_CN
    private static final String DEFAULT_LOCAL_PROPERTY_NAME = "spring.web.locale";
    // 国际化默认值
    private static final String DEFAULT_LOCAL_VAL = "zh_CN";
    // 相对于资源目录下的路径，后面拼接去掉.properties后缀名的国际化文件。例如zh_CN语言对应的文件为i18n/messages_zh_CN.properties
    private static final String MESSAGE_ROOT_PATH = "i18n/messages_";


    /**
     * 获取国际化内容。从线程上下文获取国际化配置。
     * 通常使用自定义LocaleResolver来解析请求中的Locale信息，常见的策略是通过Accept-Language头等传入
     *
     * @param code 消息键
     * @param args 参数
     * @return 国际化翻译值
     */
    public static String getMessageWithContext(String code, Object... args) {
        MessageSource messageSource = SpringUtil.getBean(MessageSource.class);
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * 获取国际化内容。从spring.mvc.locale配置获取默认国际化配置。适用于内部消息通知等固定语言的场景
     *
     * @param code 消息键
     * @param args 参数
     * @return 国际化翻译值
     */
    public static String getMessageWithDefaultLocale(String code, Object... args) {
        try {
            String defaultLocal = SpringUtil.getBean(Environment.class).getProperty(DEFAULT_LOCAL_PROPERTY_NAME,
                    DEFAULT_LOCAL_VAL);
            return getMessage(code, defaultLocal, args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return code;
        }
    }

    /**
     * 获取国际化内容。指定国际化值
     *
     * @param code  消息键
     * @param local 国际化值
     * @param args  参数
     * @return 国际化翻译值
     */
    public static String getMessage(String code, String local, Object... args) {
        try {
            // 使用SpringUtils.getBean(MessageSource.class).getMessage()会有本地默认locale缓存，某些场景会取不到期望值
            // 使用ResourceBundle直接读取本地资源文件，内部已实现kv缓存和懒加载
            ResourceBundle resourceBundle = ResourceBundle.getBundle(MESSAGE_ROOT_PATH + local);
            // 消息文本内{0}\{1}等多参数占位符自动格式化
            return MessageFormat.format(resourceBundle.getString(code), args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return code;
        }
    }
}
