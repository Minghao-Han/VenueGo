kubectl config use-context minikube
kubectl get pods
kubectl exec -it mysql-dev-6f9678d8-pkqp9 -- bash
mysql -u root -p