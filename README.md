# IDcard - Android 身份证 OCR 识别工具

![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-24-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

一个基于 Android + 腾讯云 OCR 的身份证识别示例项目，支持单张识别、批量识别、结果展示与导出分享。

> ⚠️ **安全提醒**：本项目仅供学习参考，请勿将真实的腾讯云 API 密钥提交到任何公开仓库。建议使用环境变量或 `local.properties` 管理敏感信息。

## 功能特性

- 单张身份证识别（支持正面/反面）
- 批量导入图片并逐张识别
- 批量结果页汇总成功/失败数量
- 识别结果导出
  - 单张导出为 `.txt`
  - 批量导出为 `.csv`（兼容 Excel 打开）
- 导出后可直接通过系统分享面板发送文件

## 技术栈

- Android (Java)
- Gradle
- Tencent Cloud OCR (IDCardOCR)
- Gson
- Glide
- RecyclerView

## 项目结构

```text
app/src/main/java/com/example/idcard/
├── MainActivity.java           # 主页：选择图片、发起识别（单张/批量）
├── ResultActivity.java         # 单张识别结果展示
├── BatchResultActivity.java    # 批量识别结果展示
├── OCRUtil.java                # 腾讯云 OCR 请求与签名逻辑
├── IdCardInfo.java             # OCR 结果模型与解析
├── BatchOcrResult.java         # 批量结果行模型
├── ExportUtil.java             # 结果导出与分享
└── ...                         # 其他工具与数据类
```


## 运行环境

Android Studio Hedgehog 或更高版本（建议）

JDK 8+

Android SDK:

  compileSdk 34

  targetSdk 34

  minSdk 24

可访问公网（调用腾讯云 OCR 接口）

## 快速开始
克隆项目

bash
git clone <your-repo-url>
cd IDcard
使用 Android Studio 打开项目

等待 Gradle 同步完成

配置腾讯云 OCR 密钥（见下节）

连接真机或启动模拟器后运行

## 腾讯云 OCR 配置
当前项目在 OCRUtil.java 中通过 SECRET_ID 与 SECRET_KEY 调用腾讯云 IDCardOCR 接口。

你需要替换为自己的腾讯云密钥：

登录 腾讯云控制台，获取 SecretId 和 SecretKey

修改 app/src/main/java/com/example/idcard/OCRUtil.java 中以下常量：

SECRET_ID

SECRET_KEY

如有需要可调整 X-TC-Region


## 使用说明
### 单张识别
点击"选择图片"
<img width="1080" height="1773" alt="0f2a80c2d00e6eeb13dc82c486b4e49f" src="https://github.com/user-attachments/assets/da063944-b150-4756-9bbf-31cf6ee1c145" />

选择身份证正面或反面

点击"开始识别"

点击"查看结果"进入详情页
<img width="1080" height="2120" alt="f935c5e94cfb74252145eaf5ffa388b1" src="https://github.com/user-attachments/assets/fc620a34-61fc-4b80-821c-b5436b0d316a" />


在结果页点击"导出"可分享 .txt 文件
<img width="1080" height="2204" alt="a998286bc20d893f8f96cd22de5318eb" src="https://github.com/user-attachments/assets/9da6c8fc-06ed-4b60-9781-c269145d3237" />

### 批量识别
点击"批量选择图片"
<img width="1080" height="1807" alt="8f09f05673e150db8d9192feb0879304" src="https://github.com/user-attachments/assets/2164dbab-e4b3-454c-96a4-a02ca804c688" />

为每张图片选择"正面/反面"

点击"开始批量识别"

点击"查看批量结果"
<img width="1080" height="2186" alt="d32096aabbaab7825dd9518891aba8c6" src="https://github.com/user-attachments/assets/b95c9fa4-d0e3-4fb1-ba5b-5545f69bb3e2" />

在批量结果页点击"导出"可分享 .csv 文件
<img width="1080" height="2190" alt="c774fc3cf77838ccb32f8ed3e18a5ee9" src="https://github.com/user-attachments/assets/858b3d01-e087-4d80-b920-7242e69e3978" />


## 权限说明
项目中使用了以下主要权限：

网络权限：用于请求 OCR 接口

图片读取权限：用于从相册获取身份证图片

FileProvider：用于导出文件后的安全分享

具体可查看 app/src/main/AndroidManifest.xml。

## 常见问题
识别失败 / HTTP 错误：

检查 SECRET_ID / SECRET_KEY 是否正确

检查 OCR 服务是否开通、额度是否可用

检查网络连接和接口地域配置

导出失败：

确认应用有读取图片权限

重试识别后再导出

## 贡献
欢迎提交 Issue 和 Pull Request！

Fork 本项目

创建你的特性分支 (git checkout -b feature/AmazingFeature)

提交你的更改 (git commit -m 'Add some AmazingFeature')

推送到分支 (git push origin feature/AmazingFeature)

提交 Pull Request

## 致谢
腾讯云 OCR 提供的识别服务

Glide 图片加载库

Gson JSON 解析库

## License
本项目采用 MIT License，详见 LICENSE 文件。
