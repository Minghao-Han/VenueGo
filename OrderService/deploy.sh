#!/bin/bash

# --- 1. 配置变量 ---
APP_NAME="order-service"
IMAGE_NAME="order-service"
TAG="v1"
HOST_PORT=6239
CONTAINER_PORT=6239
PROFILE=${1:-dev}

# --- 启动新容器 ---
echo "🚢 正在启动容器..."
docker run -d \
  --name ${APP_NAME} \
  --rm \
  --network venuego-net \
  -p ${HOST_PORT}:${CONTAINER_PORT} \
  -e SPRING_PROFILES_ACTIVE=${PROFILE} \
  ${IMAGE_NAME}:${TAG}

# --- 6. 结果检查 ---
sleep 2 # 等待 2 秒让应用初始化
if [ "$(docker ps -q -f name=${APP_NAME})" ]; then
    echo "✅ 部署成功！"
    echo "访问地址: http://localhost:${HOST_PORT}"
    echo "查看日志命令: docker logs -f ${APP_NAME}"
else
    echo "❌ 容器启动失败，请检查日志。"
    docker logs ${APP_NAME}
fi
