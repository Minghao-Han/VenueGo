# bash

# install helm

curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh

# install minikube

curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# start minukube with 2 nodes, 4 cpus and 8GB of memory
minikube start --cpus 4 --memory 8192 --driver=docker

# download kong ingress
# 添加仓库
helm repo add kong https://charts.konghq.com
helm repo update

# 安装 Kong (无数据库模式)
helm install kong kong/kong \
  --namespace kong --create-namespace \
  --set ingressController.enabled=true \
  --set proxy.type=LoadBalancer

# verify installation
alias kubectl="minikube kubectl --"
kubectl get pods -n kong