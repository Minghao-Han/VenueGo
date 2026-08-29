# VenueGo CI/CD Entry Point
# ──────────────────────────────────────────────────────────────
# Build targets    : make build | make build-<svc>
# K8s deploy       : make deploy
# Port-forwards    : make start-registry | make start-kong
# Full startup     : make all
# ──────────────────────────────────────────────────────────────

BUILD := bash scripts/build-service.sh

.PHONY: build build-auth build-user build-venue build-ticketing build-checkin build-order \
        deploy start-registry stop-registry start-kong stop-kong status pods all

# ── Build ──────────────────────────────────────────────────────
build:
	$(MAKE) -j6 build-auth build-user build-venue build-ticketing build-checkin build-order

build-auth:
	$(BUILD) AuthService minghaohan/venuego-auth v1

build-user:
	$(BUILD) UserService minghaohan/venuego-user v1

build-venue:
	$(BUILD) VenueService minghaohan/venuego-venue v1

build-ticketing:
	$(BUILD) TicketingService minghaohan/venuego-ticketing v1

build-checkin:
	$(BUILD) CheckInService minghaohan/venuego-checkin v1

build-order:
	$(BUILD) OrderService minghaohan/venuego-order v1

# ── Deploy ─────────────────────────────────────────────────────
deploy:
	bash minikube-deploy/deploy.sh
	@TAG=$$(cat .last-build-tag 2>/dev/null || git rev-parse --short HEAD); \
	echo "--- Updating images to tag: $$TAG ---"; \
	minikube kubectl -- set image deployment/auth-service      auth-service=minghaohan/venuego-auth:$$TAG; \
	minikube kubectl -- set image deployment/user-service      user-service=minghaohan/venuego-user:$$TAG; \
	minikube kubectl -- set image deployment/venue-service     venue-service=minghaohan/venuego-venue:$$TAG; \
	minikube kubectl -- set image deployment/ticketing-service ticketing-service=minghaohan/venuego-ticketing:$$TAG; \
	minikube kubectl -- set image deployment/checkin-service   checkin-service=minghaohan/venuego-checkin:$$TAG; \
	minikube kubectl -- set image deployment/order-service     order-service=minghaohan/venuego-order:$$TAG; \
	echo "✅ All deployments updated to $$TAG"

# ── Port-forwards ──────────────────────────────────────────────
start-registry:
	minikube addons enable registry
	nohup minikube kubectl -- port-forward -n kube-system service/registry 5000:80 \
	  >/tmp/minikube-registry.log 2>&1 &
	@echo "Registry → localhost:5000  (log: /tmp/minikube-registry.log)"

stop-registry:
	-pkill -f "port-forward -n kube-system service/registry 5000:80"
	@echo "Registry stopped"

start-kong:
	nohup minikube kubectl -- port-forward -n kong service/kong-kong-proxy 8080:80 \
	  >/tmp/minikube-kong.log 2>&1 &
	@echo "Kong → localhost:8080  (log: /tmp/minikube-kong.log)"

stop-kong:
	-pkill -f "port-forward -n kong service/kong-kong-proxy 8080:80"
	@echo "Kong stopped"

grafana:
	nohup minikube kubectl -- port-forward svc/grafana 3000:3000 \
	  >/tmp/minikube-grafana.log 2>&1 &
	@echo "Grafana → localhost:3000  (log: /tmp/minikube-grafana.log)"

status:
	@ps aux | grep "port-forward" | grep -v grep || echo "(no active port-forwards)"

pods:
	minikube kubectl -- get pods

# ── Full startup ───────────────────────────────────────────────
all: build deploy start-registry start-kong
