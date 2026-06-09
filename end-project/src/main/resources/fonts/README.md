# 中文字体文件

本目录用于存放 PDF 生成所需的中文字体文件。

## 需要的字体文件

- `SourceHanSansCN-Regular.ttf` - 思源黑体 Regular

## 下载方式

### 方式一：从 GitHub 下载
访问 [Adobe Source Han Sans](https://github.com/adobe-fonts/source-han-sans/releases)
- 下载 `SourceHanSansCN-Regular.otf` 并重命名为 `SourceHanSansCN-Regular.ttf`

### 方式二：从 Google Fonts 下载
访问 [Noto Sans SC](https://fonts.google.com/noto/specimen/Noto+Sans+SC)
- 下载 Regular 字体文件并重命名为 `SourceHanSansCN-Regular.ttf`

### 方式三：使用系统字体
Windows 系统字体目录：`C:\Windows\Fonts\`
- 可以复制 `msyh.ttc`（微软雅黑）或 `simsun.ttc`（宋体）并重命名
- 注意：.ttc 文件需要转换为 .ttf 格式

## 文件放置

将下载的字体文件放置到本目录：
```
end-project/src/main/resources/fonts/SourceHanSansCN-Regular.ttf
```

## 验证

启动应用后，访问下载发票接口测试字体是否正常加载。
如果出现字体缺失错误，请检查：
1. 字体文件名是否正确
2. 字体文件是否损坏
3. 字体文件是否支持中文
