# 配置文件轻量热更新使用说明
springboot配置文件轻量热更新，不依靠配置中心等中间件。基于最小化原则，适用于少量配置的修改

## 使用示例
需要结合bean属性注入使用。
### 1、定义配置类和配置属性
```java
@Configuration
public class QuartzConfig {

    /**
     * 定时任务全局启停控制标志位。配合配置文件热更新使用
     */
    @Value("${enableQuartz:true}")
    private Boolean enableQuartz;
}
```
### 2、热更新参数配置
```yaml
hotreload:
    prop:
      # 监听的配置文件全路径
      monitor-file: /xxx/config/application.yml
      # 配置文件更新监控间隔，单位秒
      monitor-interval-sec: 5
      # 热更新支持的属性列表，如果有多个配置用逗号分隔
      param-list: [ enableQuartz#QuartzConfig ]
```

### 3、配置项使用
在目标模块，定义值判定方法，实现业务控制。
```java
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;

private Boolean isEnableQuartz() {
    try {
        Object targetBean = SpringUtil.getBean("quartzConfig");
        return (Boolean) BeanUtil.getFieldValue(targetBean, "enableQuartz");
    } catch (Exception ignored) {
        return true;
    }
}
```

## param-list参数项说明
由于存在配置文件定义的属性名称，和bean内注入绑定的属性名称不一致的情况，因此支持以下两种格式：
+ 支持格式1：<配置文件定义的属性名称>#<spring bean名称>#<bean内对应的字段名称>
+ 支持格式2：<配置文件定义的属性名称>#<spring bean名称>