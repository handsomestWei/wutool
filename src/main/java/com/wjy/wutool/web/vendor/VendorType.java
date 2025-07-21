package com.wjy.wutool.web.vendor;

/**
 * 通用厂商类型枚举，集中配置所有支持的厂商类型及其服务实现类。
 * <p>
 * 用法说明：
 * 1. 每个枚举项代表一个厂商类型，需配置唯一
 * code、显示名称、服务实现类全限定名（serviceClassName）、注册到Spring的beanName。
 * 2. 注册器会通过反射自动实例化 serviceClassName 并注册，无需 SPI 文件和主工程依赖。
 * 3. 新增厂商时只需在此枚举中补充配置，无需改动其他代码。
 * <p>
 * 示例：
 * EXAMPLE_VENDOR("example", "示例厂商",
 * "com.example.vendor.impl.ExampleVendorServiceImpl", "exampleVendorService")
 */
public enum VendorType {
    /**
     * 示例厂商（请根据实际业务补充/替换）
     */
    EXAMPLE_VENDOR("example", "示例厂商", "com.example.vendor.impl.ExampleVendorServiceImpl", "exampleVendorService"),
    // 其他厂商类型...
    ;

    private final String code;
    private final String name;
    private final String serviceClassName;
    private final String beanName;

    VendorType(String code, String name, String serviceClassName, String beanName) {
        this.code = code;
        this.name = name;
        this.serviceClassName = serviceClassName;
        this.beanName = beanName;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    /**
     * 获取注册到Spring容器的beanName
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * 根据 code 获取厂商类型
     *
     * @param code 厂商唯一标识
     * @return VendorType 或 null
     */
    public static VendorType getByCode(String code) {
        for (VendorType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
