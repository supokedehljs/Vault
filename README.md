# Eagle素材库阅读器

安卓应用，用于读取飞牛NAS上的Eagle素材库。

## 构建APK的方法

### 方法1: GitHub Actions 自动构建（推荐）

1. 将此项目上传到你的GitHub仓库
2. 进入仓库的 Actions 页面
3. 点击 "Build Android APK" workflow
4. 点击 "Run workflow"
5. 构建完成后在 Artifacts 中下载 `EagleReader-debug.apk`

### 方法2: 本地Android Studio构建

1. 安装 [Android Studio](https://developer.android.com/studio)
2. 打开此项目文件夹
3. 等待Gradle同步完成
4. 点击菜单: Build > Build Bundle(s) / APK(s) > Build APK(s)
5. APK生成在 `app/build/outputs/apk/debug/app-debug.apk`

## 功能特性

- 连接飞牛NAS上的Eagle素材库
- 读取文件夹分类结构
- 读取图片/视频评分（star 1-5星）
- 按文件夹筛选媒体文件
- 支持SMB协议

## 使用说明

1. 安装APK到安卓手机
2. 输入飞牛NAS地址（如 `smb://192.168.1.100`）
3. 输入NAS用户名和密码
4. 点击"连接并加载素材库"
5. 选择文件夹筛选，或浏览全部文件

## Eagle库结构

```
李杰.library/
├── metadata.json     # 文件夹结构和标签
├── tags.json         # 历史标签
├── images/
│   └── [ID].info/
│       ├── metadata.json  # 单个文件元数据
│       └── [filename].jpg # 原始文件
```

## 元数据字段

| 字段 | 说明 |
|------|------|
| id | 文件唯一ID |
| name | 文件名 |
| star | 评分 (0-5) |
| folders | 所属文件夹ID数组 |
| tags | 标签数组 |
| width/height | 尺寸 |
| ext | 扩展名 |