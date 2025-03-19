# wutool

一个java代码片段收集库，针对特定场景提供轻量解决方案。

## 关于wutool

+ wutool针对特定场景提供轻量解决方案，归类放置在工程里不同的包下。
+ 和常见的工具库不同，使用wutool无需全量引用，只要按需选择代码片段拷贝使用即可。

## 片段列表

- task
    - [基于内存的动态定时任务管理。任务增删改查](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/task/dynamic/DynamicTaskManager.java)
    - [sql大in多线程并行查询模板类。自定义数据拆分和合并方法](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/task/PartitionSelectJob.java)
    - [并行执行多任务。阻塞等待所有任务都执行完成并收集结果，超时自动终止未完成任务](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/task/ParallelCallableUtil.java)
- 热更新
    - [配置文件属性热更新。声明式定义，无需侵入式编码](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/hotreload/MiniHotReloadPropComponent.java)
- 消息
    - [简单消息响应模型。将三方的异步响应包装成阻塞等待同步响应](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/msg/reactor/service/AbsMsgReactorService.java)
- ftp
    - [基于Apache Camel FTP的文件下载处理模板类。支持并发文件处理，支持处理进度持久化和重加载](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/ftp/process/AbsBizFileProcess.java)
- excel
    - [excel http附件下载、模板填充](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/excel/util/RspAttachmentHelper/.java)
    - [excel动态列写入](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/excel/util/DynamicHeadHelper/.java)
    - [excel单元格上自适应填充图片](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/excel/ImageAutoFillMergeCelHandler.java)
    - [基于阿里easy excel的包装工具类](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/excel/util/EasyExcelUtil.java)
- web
    - [tcp客户端，基于java.net.Socket实现。支持自定义编解码、优雅处理读响应超时、多包响应拆解处理](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/web/TcpClient.java)
    - [http调用链id注入过滤器。适合单体架构](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/web/filter/HttpTraceIdFilter.java)
    - [http请求控制器日志打印切面。输出请求入出参数值和耗时到日志](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/web/WebLogAspect.java)
    - [参数校验配置。可作用在http请求入参校验等](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/web/ValidatorConfig.java)
    - [i18n国际化工具类。规避某些场景下因缓存取不到期望值问题](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/web/I18nUtil.java)
    - [接口请求url unicode解码处理器](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/web/UrlPathUnicodeDecodeResolver.java)
    - mybatis
        - [基于策略自动填充时间类型字段值](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/web/mybatis/TimeMetaObjectHandler)
        - [pg数据库jsonb字段类型处理器](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/web/mybatis/PgJsonbTypeHandler)
- 工具类
    - [二维码图片生成、编码、解码](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/QrCodeUtil.java)
    - [图片处理：多图合并和加字、图片画框](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/PicUtil.java)
    - [视频处理：图片合成mp4（支持文件、链接等形式的图片）、视频http附件下载](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/MediaUtil.java)
    - [内存定时kv缓存。可避免引入额外中间件](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/MemoryTimedKvCacheUtil.java)
    - [基于时间间隔的内存kv缓存过滤器。可用于流量削峰](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/MemoryKvCacheDurationFilter.java)
    - [使用各种姿势获取资源的路径](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/ResourcePathUtil.java)
    - [简单资源操作锁](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/SimpleResourceLock.java)
- 其他配置
    - [logback日志输出配置范例。文件大小限制、滚动覆盖策略、定时清理等](https://github.com/handsomestWei/wutool/tree/main/src/main/resources/logback.xml)