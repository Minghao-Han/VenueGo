#!/bin/bash
# Apply all Kubernetes manifests. Run from any directory.
set -e

cd "$(dirname "$0")"

echo "--- Applying Kubernetes manifests ---"
minikube kubectl -- apply -f auth/jwt-secret.yaml
minikube kubectl -- apply -f apps/services.yaml
minikube kubectl -- apply -f plugins/jwt-setup.yaml
minikube kubectl -- apply -f auth/consumer.yaml
minikube kubectl -- apply -f ingress/ingress.yaml
minikube kubectl -- apply -f monitoring/prometheus-grafana.yaml
echo "✅ All manifests applied"
