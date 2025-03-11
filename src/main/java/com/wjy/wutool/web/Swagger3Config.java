package com.wjy.wutool.web;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.*;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// 启用swaggers
@EnableOpenApi
@Configuration
public class Swagger3Config {

    @Bean
    public Docket createRestApi() {
        // 返回文档摘要信息
        Docket docket = new Docket(DocumentationType.OAS_30).apiInfo(genApiInfo());
        this.appendAllApi(docket);
//        this.appendApis(docket, new String[]{"com.wjy.wutool.web"});
//        this.appendGlobalOtherInfo(docket);
        return docket;
    }

    // 生成接口信息，包括标题、联系人等
    private ApiInfo genApiInfo() {
        return new ApiInfoBuilder().title("xxx接口文档").description("xxx")
                .contact(new Contact("xx", "https://xxx", "xxx@qq.com")).version("1.0").build();
    }

    /**
     * 配置要展示的接口包。扫描全部带有ApiOperation注解的接口
     */
    private Docket appendAllApi(Docket docket) {
        docket.select().apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)).paths(PathSelectors.any());
        return docket;
    }

    /**
     * 配置要展示的接口包。指定包名，按需展示要暴露的接口
     */
    private Docket appendApis(Docket docket, String[] apiPackages) {
        Predicate<RequestHandler> selector = null;
        for (int i = 0; i < apiPackages.length; i++) {
            if (i == 0) {
                selector = RequestHandlerSelectors.basePackage(apiPackages[0]);
            } else {
                selector = selector.or(RequestHandlerSelectors.basePackage(apiPackages[i]));
            }
        }
        if (selector != null) {
            docket.select().apis(selector);
        }
        docket.select().paths(PathSelectors.any());
        return docket;
    }

    /**
     * 补充全局其他配置
     */
    private Docket appendGlobalOtherInfo(Docket docket) {
        return docket.globalRequestParameters(genGlobalRequestParameters())
                .globalResponses(HttpMethod.GET, genGlobalResponseMessage())
                .globalResponses(HttpMethod.POST, genGlobalResponseMessage());
    }

    /**
     * 生成全局通用请求参数
     */
    private List<RequestParameter> genGlobalRequestParameters() {
        List<RequestParameter> parameters = new ArrayList<>();
        parameters.add(new RequestParameterBuilder().name("version").description("客户端版本号").required(true).in(ParameterType.QUERY)
                .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING))).required(false).build());
        return parameters;
    }

    /**
     * 生成全局通用响应信息
     */
    private List<Response> genGlobalResponseMessage() {
        List<Response> responseList = new ArrayList<>();
        responseList.add(new ResponseBuilder().code("404").description("找不到资源").build());
        return responseList;
    }
}
