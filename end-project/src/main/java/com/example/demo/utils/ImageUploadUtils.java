package com.example.demo.utils;

import com.example.demo.config.UploadProperties;
import com.example.demo.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

/**
 * 图片上传工具类
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ImageUploadUtils {

    private final UploadProperties uploadProperties;

    /**
     * 校验文件是否合法
     * @param file 上传的文件
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("上传文件不能为空");
        }
        // 限制文件大小 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("文件大小不能超过 10MB");
        }
        // 校验 MIME 类型
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedType(contentType)) {
            throw new BadRequestException("不支持的文件类型: " + contentType
                    + "，允许类型: " + uploadProperties.getAllowedTypes());
        }
    }

    /**
     * 判断 MIME 类型是否允许
     */
    private boolean isAllowedType(String contentType) {
        return Arrays.asList(uploadProperties.getAllowedTypesArray())
                .contains(contentType.toLowerCase());
    }

    /**
     * 生成唯一文件名（UUID + 原始扩展名）
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    public String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 保存文件到本地并返回访问 URL
     * @param file 上传的文件
     * @return 图片访问 URL
     */
    public String saveFile(MultipartFile file) {
        try {
            // 校验
            validateFile(file);

            // 确保上传目录存在
            java.io.File uploadDir = new java.io.File(uploadProperties.getPath());
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
            log.info("生成文件名: {}", uniqueFileName);

            // 保存文件
            Path filePath = Paths.get(uploadProperties.getPath(), uniqueFileName);
            log.info("保存路径: {}", filePath);
            Files.write(filePath, file.getBytes());

            // 返回 URL
            String imageUrl = getFileUrl(uniqueFileName);
            log.info("文件名: {}, URL: {}", uniqueFileName, imageUrl);
            return imageUrl;

        } catch (IOException e) {
            throw new BadRequestException("文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param fileName 文件名
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        try {
            Path filePath = Paths.get(uploadProperties.getPath(), fileName);
            java.io.File file = filePath.toFile();
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            log.error("删除文件失败: {}, 错误: {}", fileName, e.getMessage());
            return false;
        }
    }

    /**
     * 根据文件名获取访问 URL
     * @param fileName 文件名
     * @return 完整的 HTTP URL
     */
    public String getFileUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        String urlPrefix = uploadProperties.getUrlPrefix();
        if (urlPrefix == null || urlPrefix.isEmpty()) {
            log.warn("未配置 upload.url-prefix，默认使用 /images/products/");
            urlPrefix = "/images/products/";
        }
        return urlPrefix + fileName;
    }

    /**
     * 批量保存文件
     * @param files 上传的文件数组
     * @return 文件 URL 数组
     */
    public String[] saveFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return new String[0];
        }
        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = saveFile(files[i]);
        }
        return fileNames;
    }

    /**
     * 批量删除文件
     * @param fileNames 文件名数组
     * @return 成功删除的数量
     */
    public int deleteFiles(String[] fileNames) {
        if (fileNames == null || fileNames.length == 0) {
            return 0;
        }
        int successCount = 0;
        for (String fileName : fileNames) {
            if (deleteFile(fileName)) {
                successCount++;
            }
        }
        return successCount;
    }
}
