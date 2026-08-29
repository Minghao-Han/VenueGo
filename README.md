# VenueGo

A single-city local‑events & ticketing platform built to survive **flash‑sale traffic** — event discovery, following, high‑concurrency ticket grabbing, async order/payment, QR e‑ticket check‑in, and metrics.

> Chinese requirements & architecture notes: [`Readmd.cn.md`](./Readmd.cn.md)

## Highlights

- **Oversell‑proof flash sale** — inventory decrement, per‑user purchase limit, and sale‑window check run as a single atomic Redis Lua script ([`purchase_ticket.lua`](./TicketingService/src/main/resources/lua/purchase_ticket.lua)); the DB order is created asynchronously.
- **Event‑driven consistency** — RocketMQ decouples purchase → order creation → payment callback → order timeout. Switching sends from sync to async cut purchase latency ~60ms → ~40ms.
- **Polyglot service comms** — REST for commands, **gRPC** for internal calls (venue↔ticketing inventory sync, order→venue tier lookup), **GraphQL** for public venue browsing.
- **Gateway‑enforced auth** — Kong ingress terminates JWT (`jwt-auth` plugin) and injects the user id downstream; services stay auth‑light. Public vs. protected routes split at the ingress.
- **Redis, used properly** — cache, distributed atomic ops (Lua), GEO for nearby venues, bitmap for daily sign‑in; Caffeine as L1 cache in front.
- **DDD order service** — aggregate + state machine + idempotency checker for the order lifecycle.
- **Observability** — Micrometer → Prometheus → Grafana, plus a custom AOP aspect exporting per‑endpoint DB‑query counts.
- **Load tested** — venue query ~5.3k RPS @ p99 42ms (wrk, single‑node minikube).

## Architecture

```
                 ┌──────────────┐
 React (Vite) ─> │ Kong Ingress │──JWT verify + userid inject
                 └──────┬───────┘
        ┌───────────┬───┴────┬───────────┬────────────┬───────────┐
     Auth        User      Venue      Ticketing     Order      CheckIn
   (JWT/RSA)  (profile,  (events,   (inventory,   (DDD+state  (QR, one-time
              sign-in)   GraphQL,    Redis Lua)    machine)     verify)
                         gRPC)          │  ▲           │
                                        └──RocketMQ────┘
                 MySQL 8 (primary/replica) · Redis 7 · RocketMQ · Prometheus/Grafana
```

| Service | Stack | Responsibility |
|---|---|---|
| AuthService | Spring Boot 3, Java 21, RSA/JWT | register / login / logout, token blacklist |
| UserService | Spring Boot 3, gRPC | profile, daily sign‑in (Redis bitmap) |
| VenueService | Spring Boot 3, GraphQL, Caffeine, gRPC | events / venues / ticket tiers, nearby (GEO) |
| TicketingService | Spring Boot 3, Redisson, RocketMQ, gRPC | inventory, flash‑sale purchase, payment stub |
| OrderService | Spring Boot 3, RocketMQ, state machine | order lifecycle, idempotency, timeout |
| CheckInService | Spring Boot 3 | e‑ticket QR issue & one‑time check‑in |
| frontend | React 18, Vite, react‑router 7, i18next | consumer + organizer + staff UI |

## Quick start (minikube)

```bash
# prerequisites: docker, minikube, make
make all          # build all 6 images, deploy manifests, port-forward registry + Kong
make pods         # check status
make grafana      # Grafana on :3000

# frontend
cd frontend && npm i && npm run dev
```

Individual targets: `make build-<svc>`, `make deploy`, `make start-kong`. Manifests live in [`minikube-deploy/`](./minikube-deploy/).

## API docs

Each service exposes Swagger UI at `/swagger-ui.html` (springdoc). Public entrypoints via Kong: `/api/auth/**`, `/graphql`. Protected (JWT): `/api/user`, `/api/v1/venues`, `/api/v1/tickets`, `/orders`, `/api/checkin`.

## Tech stack

Java 21 · Spring Boot 3 · MySQL 8 · Redis 7 (Redisson) · RocketMQ · gRPC · GraphQL · Kong · Prometheus + Grafana · Docker + Kubernetes (minikube) · React 18 + Vite
