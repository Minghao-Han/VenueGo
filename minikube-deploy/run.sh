#!/bin/bash
#run with： 'source run.sh' or '. run.sh'

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

SERVICE_DIRS=(
	"AuthService"
	"CheckInService"
	"OrderService"
	"TicketingService"
	"UserService"
	"VenueService"
)

for service in "${SERVICE_DIRS[@]}"; do
	chmod +x "$PROJECT_ROOT/$service/build.sh"
done

for service in "${SERVICE_DIRS[@]}"; do
	cd "$PROJECT_ROOT/$service"
	./build.sh
done


kubectl="minikube kubectl --"
# 1. 启动你的微服务 (Echo Server)
$kubectl apply -f auth/jwt-secret.yaml
$kubectl apply -f apps/services.yaml

# 2. 声明 Kong 插件逻辑 (JWT 和 Header 注入)
$kubectl apply -f plugins/jwt-setup.yaml

# 3. 创建消费者并配置公钥
$kubectl apply -f auth/consumer.yaml

# 4. 最后打开网关入口
$kubectl apply -f ingress/ingress.yaml

# enable minikube registry addon
minikube addons enable registry
# 启动 Registry 转发
alias start-registry='nohup $kubectl port-forward -n kube-system service/registry 5000:80 >/tmp/minikube-registry.log 2>&1 &'
# 停止 Registry 转发
alias stop-registry='pkill -f "port-forward -n kube-system service/registry 5000:80"'

# --- Kong 管理 ---
# 启动 Kong 转发
alias start-kong='nohup $kubectl port-forward -n kong service/kong-kong-proxy 8080:80 >/tmp/minikube-kong.log 2>&1 &'
# 停止 Kong 转发
alias stop-kong='pkill -f "port-forward -n kong service/kong-kong-proxy 8080:80"'

alias pf-status='ps aux | grep "port-forward" | grep -v grep'

start-registry
start-kong