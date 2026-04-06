@echo off
REM Eagle素材库阅读器 - 构建脚本
REM 需要: Java JDK 17+, Android SDK

echo ===== Eagle素材库阅读器 构建脚本 =====

REM 检查Java
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未安装Java JDK
    echo 请从 https://adoptium.net 下载安装
    pause
    exit /b 1
)

REM 检查Android SDK
if not exist "%ANDROID_HOME%\platforms\android-34" (
    echo 错误: 未安装Android SDK Platform 34
    echo 请运行: sdkmanager "platforms;android-34" "build-tools;34.0.0"
    pause
    exit /b 1
)

REM 下载jcifs依赖
if not exist "app\libs\jcifs-1.3.17.jar" (
    echo 正在下载jcifs依赖...
    mkdir app\libs 2>nul
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/net/java/dev/jcifs/jcifs/1.3.17/jcifs-1.3.17.jar' -OutFile 'app\libs\jcifs-1.3.17.jar'"
)

REM 构建APK
echo 正在构建APK...
gradlew.bat assembleDebug --no-daemon

if errorlevel 1 (
    echo 构建失败！
    pause
    exit /b 1
)

echo ===== 构建完成 =====
echo APK位置: app\build\outputs\apk\debug\app-debug.apk
echo.
pause