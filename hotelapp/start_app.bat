@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 酒店管理系统 Windows 启动脚本
echo =========================================
echo      酒店管理系统启动脚本 v2.0
echo =========================================

:: 进入脚本所在目录
cd /d "%~dp0"

:menu
echo.
echo 请选择要执行的操作：
echo 1) 编译并运行登录界面 (推荐)
echo 2) 只编译源代码
echo 3) 只运行登录界面
echo 4) 运行主菜单
echo 5) 运行房间统计测试
echo 6) 运行预订功能测试
echo 7) 运行登录功能测试
echo 8) 运行数据库连接全面测试
echo 9) 退出
echo.
set /p choice="请输入选项 (1-9): "

if "%choice%"=="1" goto compile_and_run
if "%choice%"=="2" goto compile_only
if "%choice%"=="3" goto run_login
if "%choice%"=="4" goto run_main_menu
if "%choice%"=="5" goto run_room_stats_test
if "%choice%"=="6" goto run_booking_test
if "%choice%"=="7" goto run_login_test
if "%choice%"=="8" goto run_database_connection_test
if "%choice%"=="9" goto exit
echo 无效选项，请输入 1-9
goto menu

:compile_and_run
echo 正在编译并启动应用程序...
call :compile_all
if %errorlevel%==0 (
    call :run_login
)
goto continue

:compile_only
call :compile_all
goto continue

:run_login
echo 正在启动登录界面...
java -cp "lib/*;bin" com.hotel.app.LoginPage
goto continue

:run_main_menu
echo 正在启动主菜单...
java -cp "lib/*;bin" com.hotel.app.MainMenu
goto continue

:run_room_stats_test
echo 正在运行房间统计测试...
java -cp "lib/*;bin" test_room_stats
goto continue

:run_booking_test
echo 正在运行预订功能测试...
java -cp "lib/*;bin" BookingTestFix
goto continue

:run_login_test
echo 正在运行登录功能测试...
java -cp "lib/*;bin" com.hotel.app.LoginTestFix
goto continue

:run_database_connection_test
echo 正在运行数据库连接全面测试...
java -cp "lib/*;bin" 数据库连接全面测试
goto continue

:compile_all
echo 正在编译Java源文件...
if exist bin rmdir /s /q bin
mkdir bin

javac -cp "lib/*" -d bin src/com/database/helper/*.java src/com/hotel/ui/*.java src/com/hotel/auth/*.java src/com/hotel/system/*.java src/com/hotel/user/*.java src/com/hotel/room/*.java src/com/hotel/book/*.java src/com/hotel/app/*.java src/com/all/search/*.java *.java >nul 2>&1

if %errorlevel%==0 (
    echo ✅ 编译成功！
    exit /b 0
) else (
    echo ❌ 编译失败！
    exit /b 1
)

:continue
echo.
echo 按任意键继续...
pause >nul
goto menu

:exit
echo 谢谢使用！再见！
exit /b 0 