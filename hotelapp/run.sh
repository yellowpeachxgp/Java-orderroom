#!/bin/bash

# 创建编译输出目录
mkdir -p ./bin

# 编译项目
echo "编译项目..."
javac -cp "./lib/mysql-connector-j-9.3.0.jar:./src" -d ./bin $(find ./src -name "*.java")

# 如果编译成功，则运行项目
if [ $? -eq 0 ]; then
    echo "编译成功，正在启动应用..."
    java -cp "./lib/mysql-connector-j-9.3.0.jar:./bin" com.hotel.app.LoginPage
else
    echo "编译失败，请检查错误。"
fi 