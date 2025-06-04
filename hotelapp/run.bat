@echo off
echo 创建编译输出目录...
if not exist "bin" mkdir bin

echo 编译项目...
javac -cp "./lib/mysql-connector-j-9.3.0.jar;./src" -d ./bin src/com/hotel/app/LoginPage.java

if %errorlevel% equ 0 (
    echo 编译成功，正在启动应用...
    java -cp "./lib/mysql-connector-j-9.3.0.jar;./bin" com.hotel.app.LoginPage
) else (
    echo 编译失败，请检查错误。
    pause
) 