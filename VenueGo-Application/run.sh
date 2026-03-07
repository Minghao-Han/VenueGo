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
$kubectl port-forward -n kong service/kong-kong-proxy 8080:80