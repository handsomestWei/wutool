package com.wjy.wutool.web.vendor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 通用厂商服务注册器，支持集中配置、反射自动注册和多厂商服务查找。
 * <p>
 * 用法说明：
 * 1. 所有厂商类型及其服务实现类在 VendorType 枚举中集中配置。
 * 2. 本注册器类加载时会自动遍历 VendorType，通过反射实例化 serviceClassName 并注册。
 * 3. 新增厂商时只需在 VendorType 枚举中补充配置，无需改动其他代码。
 * <p>
 * 递归依赖注入：
 * - 反射实例化后，自动递归注入所有 @Autowired 字段依赖，支持多层依赖链。
 * <p>
 * 传统 Java SPI 机制的局限与本地开发调试缺点：
 * - 需要在 META-INF/services 目录下维护 SPI 文件，新增实现类时需手动编辑，易出错。
 * - 新增 SPI 实现类后，主工程必须声明依赖相关 jar，否则 ServiceLoader 无法发现实现，导致“侵入式”修改主工程 pom.xml。
 * - 本地开发调试时，IDEA 等工具对 SPI 文件的编译和 classpath 管理不友好，容易出现找不到实现、热加载失效等问题。
 * - SPI 机制不支持动态卸载/热更新，调试体验较差。
 * <p>
 * 反射注册方式的缺点：
 * - 通过 Class.forName/newInstance 反射实例化对象时，无法自动注入 Spring 容器中的依赖（如 @Autowired
 * 字段不会生效），AOP 代理等也会失效。
 * - 反射实例化的对象生命周期、管理权不归 Spring 容器，无法享受完整的 Spring 管理特性。
 * <p>
 * 本方案的改进：
 * - 反射实例化后，调用 getBeanFactory().autowireBean(instance)
 * 可自动完成依赖注入，弥补了反射方式无法注入依赖的缺陷。
 * - 通过 getBeanFactory().registerSingleton(beanName, instance) 将对象注册为 Spring 单例
 * Bean，后续可通过 Spring 容器获取和管理。
 * <p>
 * 本方案优势：
 * - 只需在 VendorType 枚举中集中配置实现类名，无需 SPI 文件和主工程依赖声明。
 * - 支持插件 jar 动态加载，零侵入主工程，便于扩展和维护。
 * - 反射注册机制结合 Spring 注入，兼顾灵活性和依赖管理。
 * <p>
 *
 * 注意事项：
 * - 服务实现类需继承 VendorService 并实现。
 * - 若反射实例化失败会有日志提示，请确保实现类存在且有无参构造方法。
 * - 反射实例化后，自动通过 Spring 官方 API 注入依赖并注册为单例 Bean。
 *
 * 示例配置：
 * VendorType.EXAMPLE_VENDOR("example", "示例厂商",
 * "com.example.vendor.impl.ExampleVendorServiceImpl")
 */
@Slf4j
@Component
public class VendorServiceRegistry implements ApplicationContextAware {
    private static final Map<VendorType, VendorService> vendorServiceMap = new HashMap<>();
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
        // Spring上下文准备好后再注册
        loadAndRegisterVendorServicesByReflection();
    }

    private static ConfigurableListableBeanFactory getBeanFactory() {
        return (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
    }

    /**
     * 递归依赖注入工具方法，对对象及其依赖链上的所有@Autowired字段递归调用autowireBean。
     * 支持多层依赖链递归注入。
     */
    private static void recursiveAutowire(Object instance, ConfigurableListableBeanFactory beanFactory,
            Set<Object> visited) {
        if (instance == null || visited.contains(instance))
            return;
        visited.add(instance);
        beanFactory.autowireBean(instance);
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(instance);
                    if (fieldValue != null) {
                        log.debug("VendorServiceRegistry - 递归注入依赖: {}.{} -> {}", instance.getClass().getName(),
                                field.getName(), fieldValue.getClass().getName());
                        recursiveAutowire(fieldValue, beanFactory, visited);
                    } else {
                        log.debug("VendorServiceRegistry - 递归注入依赖: {}.{} -> null", instance.getClass().getName(),
                                field.getName());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("VendorServiceRegistry - 递归注入依赖时访问字段异常: {}.{}", instance.getClass().getName(),
                            field.getName(), e);
                }
            }
        }
    }

    /**
     * 注册厂商服务
     *
     * @param vendorType 厂商类型
     * @param service    厂商服务实例
     */
    public static void registerVendorService(VendorType vendorType, VendorService service) {
        vendorServiceMap.put(vendorType, service);
        log.info("VendorServiceRegistry - VendorService registered: vendorType={}, service={}", vendorType,
                service.getClass().getName());
    }

    /**
     * 获取指定厂商类型的服务实例
     *
     * @param vendorType 厂商类型
     * @return 服务实例或null
     */
    public static VendorService getVendorService(VendorType vendorType) {
        return vendorServiceMap.get(vendorType);
    }

    /**
     * 检查指定厂商服务是否可用
     *
     * @param vendorType 厂商类型
     * @return 是否已注册
     */
    public static boolean isVendorServiceAvailable(VendorType vendorType) {
        return vendorServiceMap.containsKey(vendorType);
    }

    /**
     * 获取所有已注册的厂商类型
     *
     * @return 已注册厂商类型数组
     */
    public static VendorType[] getAvailableVendorTypes() {
        return vendorServiceMap.keySet().toArray(new VendorType[0]);
    }

    /**
     * 获取已注册的厂商服务数量
     *
     * @return 数量
     */
    public static int getRegisteredServiceCount() {
        return vendorServiceMap.size();
    }

    /**
     * 清除所有注册的厂商服务
     */
    public static void clearAllServices() {
        vendorServiceMap.clear();
    }

    /**
     * 移除指定厂商的服务
     *
     * @param vendorType 厂商类型
     * @return 被移除的服务实例
     */
    public static VendorService removeVendorService(VendorType vendorType) {
        return vendorServiceMap.remove(vendorType);
    }

    /**
     * 通过反射自动加载并注册所有厂商服务实现。
     * 只需在 VendorType 枚举中配置 serviceClassName 即可。
     * 反射实例化后自动注入依赖并注册为 Spring 单例 Bean。
     */
    public static void loadAndRegisterVendorServicesByReflection() {
        for (VendorType vendorType : VendorType.values()) {
            String className = vendorType.getServiceClassName();
            if (className == null || className.isEmpty()) {
                log.warn("VendorServiceRegistry - VendorType {} 未配置 serviceClassName，跳过注册。", vendorType);
                continue;
            }
            try {
                Class<?> clazz = Class.forName(className);
                Object instance = clazz.getDeclaredConstructor().newInstance();
                if (!(instance instanceof VendorService)) {
                    log.error("VendorServiceRegistry - {} 不是 VendorService 的实现，跳过注册。", className);
                    continue;
                }
                // 递归注入依赖
                recursiveAutowire(instance, getBeanFactory(), new HashSet<>());
                // 注册为 Spring 单例 Bean
                String beanName = vendorType.getBeanName();
                getBeanFactory().initializeBean(instance, beanName);
                getBeanFactory().registerSingleton(beanName, instance);
                VendorService service = (VendorService) instance;
                registerVendorService(vendorType, service);
            } catch (Exception e) {
                log.error("VendorServiceRegistry - 反射实例化 {} 失败，无法注册厂商服务: {}", className, e.getMessage(), e);
            }
        }
    }
}
