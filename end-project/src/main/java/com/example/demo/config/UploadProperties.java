package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {

    /** 本地存储路径 */
    private String path;

    /** 访问 URL 前缀 */
    private String urlPrefix;

    /** 允许的 MIME 类型（逗号分隔） */
    private String allowedTypes;

    /**
     * @return 允许类型数组
     */
    public String[] getAllowedTypesArray() {
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            return new String[0];
        }
        return allowedTypes.split(",");
    }
}
