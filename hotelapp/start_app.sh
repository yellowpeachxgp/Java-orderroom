#!/bin/bash

# 酒店管理系统启动脚本
# 支持自动编译和运行

echo "========================================="
echo "     酒店管理系统启动脚本 v2.0"
echo "========================================="

# 进入正确的目录
cd "$(dirname "$0")"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 函数：编译所有Java文件
compile_all() {
    echo -e "${YELLOW}正在编译Java源文件...${NC}"
    
    # 清理旧的class文件
    rm -rf bin/*
    
    # 编译所有源文件
    javac -cp "lib/*" -d bin \
        src/com/database/helper/*.java \
        src/com/hotel/ui/*.java \
        src/com/hotel/auth/*.java \
        src/com/hotel/system/*.java \
        src/com/hotel/user/*.java \
        src/com/hotel/room/*.java \
        src/com/hotel/book/*.java \
        src/com/hotel/app/*.java \
        src/com/all/search/*.java \
        *.java 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 编译成功！${NC}"
        return 0
    else
        echo -e "${RED}❌ 编译失败！${NC}"
        return 1
    fi
}

# 函数：运行登录界面
run_login() {
    echo -e "${BLUE}正在启动登录界面...${NC}"
    java -cp "lib/*:bin" com.hotel.app.LoginPage
}

# 函数：运行主菜单（需要先登录）
run_main_menu() {
    echo -e "${BLUE}正在启动主菜单...${NC}"
    java -cp "lib/*:bin" com.hotel.app.MainMenu
}

# 函数：运行房间统计测试
run_room_stats_test() {
    echo -e "${BLUE}正在运行房间统计测试...${NC}"
    java -cp "lib/*:bin" test_room_stats
}

# 函数：运行预订测试
run_booking_test() {
    echo -e "${BLUE}正在运行预订功能测试...${NC}"
    java -cp "lib/*:bin" BookingTestFix
}

# 函数：运行登录测试
run_login_test() {
    echo -e "${BLUE}正在运行登录功能测试...${NC}"
    java -cp "lib/*:bin" com.hotel.app.LoginTestFix
}

# 函数：运行数据库连接全面测试
run_database_connection_test() {
    echo -e "${BLUE}正在运行数据库连接全面测试...${NC}"
    java -cp "lib/*:bin" 数据库连接全面测试
}

# 主菜单
show_menu() {
    echo ""
    echo "请选择要执行的操作："
    echo "1) 编译并运行登录界面 (推荐)"
    echo "2) 只编译源代码"
    echo "3) 只运行登录界面"
    echo "4) 运行主菜单"
    echo "5) 运行房间统计测试"
    echo "6) 运行预订功能测试"
    echo "7) 运行登录功能测试"
    echo "8) 运行数据库连接全面测试"
    echo "9) 退出"
    echo ""
    echo -n "请输入选项 (1-9): "
}

# 主循环
while true; do
    show_menu
    read choice
    
    case $choice in
        1)
            echo -e "${YELLOW}正在编译并启动应用程序...${NC}"
            if compile_all; then
                run_login
            fi
            ;;
        2)
            compile_all
            ;;
        3)
            run_login
            ;;
        4)
            run_main_menu
            ;;
        5)
            run_room_stats_test
            ;;
        6)
            run_booking_test
            ;;
        7)
            run_login_test
            ;;
        8)
            run_database_connection_test
            ;;
        9)
            echo -e "${GREEN}谢谢使用！再见！${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}无效选项，请输入 1-9${NC}"
            ;;
    esac
    
    echo ""
    echo -e "${YELLOW}按回车键继续...${NC}"
    read
done 