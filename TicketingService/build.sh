#!/bin/bash

# --- 1. 配置变量 ---
APP_NAME="ticketing-service"
IMAGE_NAME="ticketing-service"
TAG="v1"

echo "🚀 开始部署 ${APP_NAME}..."

# --- 3. 清理旧容器 ---
# 检查是否存在同名容器，如果有则停止并删除
if [ "$(docker ps -aq -f name=${APP_NAME})" ]; then
    echo "Stopping and removing existing container: ${APP_NAME}"
    docker stop ${APP_NAME}
    docker rm ${APP_NAME}
fi

# --- 4. 构建镜像 ---
echo "🔨 正在构建镜像 ${IMAGE_NAME}:${TAG}..."
docker build -t ${IMAGE_NAME}:${TAG} .

if [ $? -ne 0 ]; then
    echo "❌ 镜像构建失败，请检查 Dockerfile！"
    exit 1
fi
