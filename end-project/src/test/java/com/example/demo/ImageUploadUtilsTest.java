package com.example.demo;

import com.example.demo.config.UploadProperties;
import com.example.demo.exception.BadRequestException;
import com.example.demo.utils.ImageUploadUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ImageUploadUtils 单元测试
 */
class ImageUploadUtilsTest {

    private ImageUploadUtils imageUploadUtils;
    private UploadProperties uploadProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        uploadProperties = new UploadProperties();
        uploadProperties.setPath(tempDir.toString() + "/");
        uploadProperties.setUrlPrefix("/images/products/");
        uploadProperties.setAllowedTypes("image/jpeg,image/png,image/jpg,image/webp");
        imageUploadUtils = new ImageUploadUtils(uploadProperties);
    }

    // ==================== validateFile ====================

    @Nested
    @DisplayName("validateFile — 文件校验")
    class ValidateFileTests {

        @Test
        @DisplayName("正常图片文件 — 校验通过")
        void shouldPassForValidImage() {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
            assertDoesNotThrow(() -> imageUploadUtils.validateFile(file));
        }

        @Test
        @DisplayName("空文件 — 抛出 BadRequestException")
        void shouldFailForEmptyFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", new byte[0]);
            assertThrows(BadRequestException.class, () -> imageUploadUtils.validateFile(file));
        }

        @Test
        @DisplayName("null 文件 — 抛出 BadRequestException")
        void shouldFailForNullFile() {
            assertThrows(BadRequestException.class, () -> imageUploadUtils.validateFile(null));
        }

        @Test
        @DisplayName("文件超大（>10MB）— 抛出 BadRequestException")
        void shouldFailForOversizedFile() {
            byte[] bigData = new byte[11 * 1024 * 1024]; // 11MB
            MockMultipartFile file = new MockMultipartFile(
                    "image", "big.jpg", "image/jpeg", bigData);
            assertThrows(BadRequestException.class, () -> imageUploadUtils.validateFile(file));
        }

        @Test
        @DisplayName("不允许的 MIME 类型 — 抛出 BadRequestException")
        void shouldFailForDisallowedType() {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.txt", "text/plain", "hello".getBytes());
            assertThrows(BadRequestException.class, () -> imageUploadUtils.validateFile(file));
        }
    }

    // ==================== generateUniqueFileName ====================

    @Nested
    @DisplayName("generateUniqueFileName — 文件名生成")
    class GenerateFileNameTests {

        @Test
        @DisplayName("保留原始扩展名")
        void shouldPreserveExtension() {
            String name = imageUploadUtils.generateUniqueFileName("photo.jpg");
            assertTrue(name.endsWith(".jpg"));
            assertTrue(name.length() > 4); // UUID + .jpg
        }

        @Test
        @DisplayName("无扩展名时返回纯 UUID")
        void shouldHandleNoExtension() {
            String name = imageUploadUtils.generateUniqueFileName("Makefile");
            assertFalse(name.contains("."));
            assertEquals(32, name.length()); // UUID without hyphens
        }

        @Test
        @DisplayName("null 文件名返回纯 UUID")
        void shouldHandleNullFilename() {
            String name = imageUploadUtils.generateUniqueFileName(null);
            assertEquals(32, name.length());
        }

        @Test
        @DisplayName("每次生成不同文件名")
        void shouldGenerateUniqueNames() {
            String name1 = imageUploadUtils.generateUniqueFileName("test.jpg");
            String name2 = imageUploadUtils.generateUniqueFileName("test.jpg");
            assertNotEquals(name1, name2);
        }
    }

    // ==================== saveFile ====================

    @Nested
    @DisplayName("saveFile — 保存文件")
    class SaveFileTests {

        @Test
        @DisplayName("正常保存并返回 URL")
        void shouldSaveAndReturnUrl() throws IOException {
            byte[] content = "fake image data".getBytes();
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.png", "image/png", content);

            String url = imageUploadUtils.saveFile(file);

            assertNotNull(url);
            assertTrue(url.startsWith("/images/products/"));
            assertTrue(url.endsWith(".png"));

            // 验证文件确实保存到了磁盘
            String fileName = url.substring("/images/products/".length());
            Path savedFile = tempDir.resolve(fileName);
            assertTrue(Files.exists(savedFile));
            assertArrayEquals(content, Files.readAllBytes(savedFile));
        }

        @Test
        @DisplayName("自动创建上传目录")
        void shouldCreateUploadDir() {
            uploadProperties.setPath(tempDir.resolve("new-dir").toString() + "/");
            byte[] content = "data".getBytes();
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", content);

            String url = imageUploadUtils.saveFile(file);
            assertNotNull(url);
        }

        @Test
        @DisplayName("空文件抛出异常")
        void shouldFailForEmptyFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", new byte[0]);
            assertThrows(BadRequestException.class, () -> imageUploadUtils.saveFile(file));
        }
    }

    // ==================== deleteFile ====================

    @Nested
    @DisplayName("deleteFile — 删除文件")
    class DeleteFileTests {

        @Test
        @DisplayName("删除已存在的文件")
        void shouldDeleteExistingFile() throws IOException {
            // 先创建文件
            Path file = tempDir.resolve("to-delete.jpg");
            Files.write(file, "data".getBytes());

            assertTrue(imageUploadUtils.deleteFile("to-delete.jpg"));
            assertFalse(Files.exists(file));
        }

        @Test
        @DisplayName("删除不存在的文件返回 false")
        void shouldReturnFalseForNonExistent() {
            assertFalse(imageUploadUtils.deleteFile("nonexistent.jpg"));
        }

        @Test
        @DisplayName("null 文件名返回 false")
        void shouldReturnFalseForNull() {
            assertFalse(imageUploadUtils.deleteFile(null));
        }

        @Test
        @DisplayName("空文件名返回 false")
        void shouldReturnFalseForEmpty() {
            assertFalse(imageUploadUtils.deleteFile(""));
        }
    }

    // ==================== getFileUrl ====================

    @Nested
    @DisplayName("getFileUrl — 获取 URL")
    class GetFileUrlTests {

        @Test
        @DisplayName("正常拼接 URL")
        void shouldConcatUrl() {
            String url = imageUploadUtils.getFileUrl("abc.jpg");
            assertEquals("/images/products/abc.jpg", url);
        }

        @Test
        @DisplayName("null 文件名返回 null")
        void shouldReturnNullForNull() {
            assertNull(imageUploadUtils.getFileUrl(null));
        }

        @Test
        @DisplayName("空文件名返回 null")
        void shouldReturnNullForEmpty() {
            assertNull(imageUploadUtils.getFileUrl(""));
        }
    }
}
