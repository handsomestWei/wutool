# wutool
一个java代码片段收集库，针对特定场景提供轻量解决方案。

## 关于wutool
+ wutool针对特定场景提供轻量解决方案，归类放置在工程里不同的包下。
+ 和常见的工具库不同，使用wutool无需全量引用，只要按需选择代码片段拷贝使用即可。

## 片段列表
- task
    - [基于内存的动态定时任务管理。任务增删改查](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/task/dynamic/DynamicTaskManager.java)
    - [sql大in多线程并行查询模板类。自定义数据拆分和合并方法](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/task/PartitionSelectJob.java)
- 热更新
    - [配置文件属性热更新。声明式定义，无需侵入式编码](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/hotreload/MiniHotReloadPropComponent.java)
- excel
    - [excel http附件下载、模板填充](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/excel/util/RspAttachmentHelper/.java)
    - [excel动态列写入](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/excel/util/DynamicHeadHelper/.java)
    - [excel单元格上自适应填充图片](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/excel/ImageAutoFillMergeCelHandler.java)
    - [基于阿里easy excel的包装工具类](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/excel/util/EasyExcelUtil.java)
- 过滤器
    - [http调用链id注入过滤器。适合单体架构](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/filter/HttpTraceIdFilter.java)
- 工具类
    - [二维码图片生成、编码、解码](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/QrCodeUtil.java)
    - [图片处理：多图合并和加字、图片画框](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/PicUtil.java)
    - [视频处理：图片合成mp4（支持文件、链接等形式的图片）、视频http附件下载](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/MediaUtil.java)
    - [内存定时kv缓存。可避免引入额外中间件](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/MemoryTimedKvCacheUtil.java)
    - [基于时间间隔的内存kv缓存过滤器。可用于流量削峰](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/MemoryKvCacheDurationFilter.java)
    - [使用各种姿势获取资源的路径](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/ResourcePathUtil.java)
    - [从spring上下文容器获取指定bean](https://github.com/handsomestWei/wutool/tree/main/src/main/java/com/wjy/wutool/util/SpringBeanUtil.java)
- 其他配置
    - [logback日志输出配置范例。文件大小限制、滚动覆盖策略、定时清理等](https://github.com/handsomestWei/wutool/tree/main/src/main/resources/logback.xml)