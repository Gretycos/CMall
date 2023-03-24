package com.tsong.cmall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 全局配置
 * @Author Tsong
 * @Date 2023/3/24 19:15
 */
@Component
@ConfigurationProperties(prefix = "project")
public class ProjectConfig {
    /**
     * 项目名称
     */
    private static String name;
    /**
     * 上传文件路径
     */
    private static String fileUploadPath;
    /**
     * 订单超期未支付时间，单位秒
     */
    private static Integer orderUnpaidOverTime;

    /**
     * 服务端访问路径
     */
    private static String serverUrl;

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        ProjectConfig.name = name;
    }

    public static String getFileUploadPath() {
        return fileUploadPath;
    }

    public static void setFileUploadPath(String fileUploadPath) {
        ProjectConfig.fileUploadPath = fileUploadPath;
    }

    public static Integer getOrderUnpaidOverTime() {
        return orderUnpaidOverTime;
    }

    public static void setOrderUnpaidOverTime(Integer orderUnpaidOverTime) {
        ProjectConfig.orderUnpaidOverTime = orderUnpaidOverTime;
    }

    public static String getServerUrl() {
        return serverUrl;
    }

    public static void setServerUrl(String serverUrl) {
        ProjectConfig.serverUrl = serverUrl;
    }
}
