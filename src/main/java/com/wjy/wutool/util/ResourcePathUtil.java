package com.wjy.wutool.util;

import java.io.File;
import java.io.IOException;

/**
 * 使用各种姿势获取资源的路径
 */
public class ResourcePathUtil {

    // 获取类运行时所属jar包路径。用于定位运行时是否有同名包冲突
    public static String getRunTimeJarPath(Class clazz) {
        return clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
    }

    // 获取类加载的根路径
    public static String getClassRootPath(Class clazz) {
        return clazz.getResource("/").getPath();
    }

    // 获取类所在项目路径
    public static String getClassProjectPath(Class clazz) {
        return clazz.getResource("").getPath();
    }

    // 获取当前项目class路径
    public static String getCurrentClassPath(Class clazz) {
        return clazz.getClassLoader().getResource("").getPath();
    }

    // 获取项目路径
    public static String getProjectPathV1() throws IOException {
        return new File("").getCanonicalPath();
    }

    // 获取项目路径
    public static String getProjectPathV2() {
        return System.getProperty("user.dir");
    }

    // 获取所有class路径
    public static String getAllClassPath() {
        return System.getProperty("java.class.path");
    }

    // 获取指定资源目录全路径
    public static String getResourceFilePath(String resourceName) {
        try {
            if (!resourceName.contains("/")) {
                resourceName = "/" + resourceName + "/";
            }
            String filePath = ResourcePathUtil.class.getResource(resourceName).getPath();
            String os = System.getProperty("os.name");
            if (os.toLowerCase().startsWith("win")) {
                // win环境下路径格式转换，适用于javacv等c库调用时传入
                // 如/D:/xx/x => D:\xx\x
                filePath = filePath.replaceFirst("/", "");
                filePath = filePath.replaceAll("/", "\\\\");
            }
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
