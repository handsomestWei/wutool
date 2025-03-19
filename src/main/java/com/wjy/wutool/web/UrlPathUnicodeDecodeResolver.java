package com.wjy.wutool.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 接口请求url unicode解码处理器。可用于某些特殊场景下url unicode码没有按期望转换成中文
 * 使用步骤：
 * 1）实现WebMvcConfigurer接口，注册自定义url资源处理器
 * 2）启用链式调用，并添加自定义处理registry.resourceChain(true).addResolver(new UrlPathUnicodeDecodeResolver());
 *
 * @author weijiayu
 * @date 2025/3/18 10:36
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addResourceHandlers
 */
@Slf4j
public class UrlPathUnicodeDecodeResolver extends PathResourceResolver {

    @Override
    @Nullable
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
        try {
            resourcePath = URLDecoder.decode(resourcePath, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return super.getResource(resourcePath, location);
    }
}
