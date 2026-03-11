#!/bin/bash

# --- 1. 配置变量 ---
APP_NAME="venue-service"
IMAGE_NAME="venue-service"
TAG="v1"
REGISTRY="localhost:32770"
FULL_IMAGE="${REGISTRY}/${IMAGE_NAME}:${TAG}"

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

# --- 5. 打本地仓库标签并推送 ---
echo "🏷️  正在打标签 ${FULL_IMAGE}..."
docker tag ${IMAGE_NAME}:${TAG} ${FULL_IMAGE}

echo "📤 正在推送 ${FULL_IMAGE}..."
docker push ${FULL_IMAGE}

if [ $? -ne 0 ]; then
    echo "❌ 镜像推送失败，请检查本地仓库是否可用（${REGISTRY}）。"
    exit 1
fi

echo "✅ 构建并推送完成: ${FULL_IMAGE}"
