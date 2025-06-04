#!/bin/bash

# 酒店预订系统启动脚本

echo "=== 酒店预订系统启动脚本 ==="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到Java环境，请先安装Java"
    exit 1
fi

# 检查必要文件
if [ ! -f "lib/mysql-connector-j-9.3.0.jar" ]; then
    echo "❌ 错误：未找到MySQL驱动文件"
    echo "请确保 lib/mysql-connector-j-9.1.0.jar 文件存在"
    exit 1
fi

# 编译Java文件
echo "🔧 编译Java文件..."
if ! javac -cp "lib/*:bin" -d bin src/com/hotel/book/SimpleRoomBooking.java; then
    echo "❌ 编译失败"
    exit 1
fi

# 获取用户名
read -p "请输入您的用户名 (默认: GUEST): " username
if [ -z "$username" ]; then
    username="GUEST"
fi

echo "📱 启动预订系统，用户: $username"
echo "✨ 正在启动图形界面..."

# 启动预订系统
java -cp "lib/*:bin" com.hotel.book.SimpleRoomBooking "$username"

echo "预订系统已关闭" 