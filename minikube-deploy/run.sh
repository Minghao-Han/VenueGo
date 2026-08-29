#!/bin/bash
# Legacy entry point — delegates to deploy.sh.
# Prefer using the Makefile at the project root:
#   make deploy          → apply k8s manifests
#   make start-registry  → port-forward registry to :5000
#   make start-kong      → port-forward Kong to :8080
#   make all             → build + deploy + start port-forwards
exec bash "$(dirname "$0")/deploy.sh"
