# Order Service Microservice

A production-grade Order Service microservice for an event ticketing platform, built with Spring Boot 4.0.3 and following COLA architecture patterns.

## Architecture Overview

### Tech Stack
- **Framework**: Spring Boot 4.0.3
- **Architecture**: COLA (Clean Object-Oriented Layered Architecture)
- **State Machine**: COLA StateMachine (not Spring StateMachine)
- **Message Queue**: RocketMQ 5.x
- **Cache**: Redis (idempotency keys)
- **Concurrency**: Optimistic locking with version field
- **ORM**: MyBatis-Plus
- **Database**: MySQL

### Project Structure

```
├── adapter/              # REST controllers, MQ consumers
│   ├── web/
│   │   └── OrderController.java
│   └── mq/
│       ├── OrderTimeoutConsumer.java
│       └── PayCallbackConsumer.java
├── app/                  # Application layer (use cases)
│   ├── command/          # Command objects
│   ├── dto/             # Data transfer objects
│   └── service/         # Command service (orchestration only)
├── domain/              # Domain layer (business logic)
│   ├── order/
│   │   ├── aggregate/   # OrderAggregate (domain model)
│   │   ├── enums/       # OrderStatus, OrderEvent
│   │   └── event/       # Domain events
│   └── statemachine/    # COLA StateMachine
│       ├── OrderStateMachineBuilder.java
│       ├── condition/   # State transition guards
│       └── action/      # State transition actions
├── infrastructure/      # Infrastructure layer
│   ├── config/         # Configuration beans
│   ├── repository/      # Data persistence
│   ├── mq/             # Message queue integration
│   ├── payment/        # Payment service integration
│   └── idempotent/     # Idempotency checking
└── common/             # Common utilities
    ├── config/         # AppProperties
    ├── exception/      # Exception handling
    ├── response/       # Response wrapper
    └── util/          # TimeProvider interface
```

## Key Design Patterns

### 1. COLA Architecture

The service strictly separates concerns across four layers:

- **Adapter Layer**: REST endpoints and message consumers. No business logic.
- **App Layer**: Use case orchestration. Routes commands, loads data, fires events.
- **Domain Layer**: Pure business logic. Business rules live in OrderAggregate and StateMachine conditions.
- **Infrastructure Layer**: Technical concerns. Database access, external service calls, caching.

### 2. Order State Machine

A complete, deterministic state machine is the single source of truth for all valid transitions:

```
PENDING_PAY ---PAY---> PAID ---USE---> USED
    |
    +--TIMEOUT---> TIMEOUT_CANCELLED
    |
    +--CANCEL---> CANCELLED

TIMEOUT_CANCELLED --PAY--> REFUNDING (late payment)
CANCELLED --PAY--> REFUNDING (late payment)
PAID --REFUND_DONE--> CANCELLED
```

**Key Design**: PAY events can transition from TIMEOUT_CANCELLED or CANCELLED to REFUNDING. This handles the race condition where a payment callback arrives after timeout or user cancellation, eliminating the need for a distributed lock.

### 3. Optimistic Locking

Every order row has a `version` integer column. Concurrent updates follow strict patterns:

```sql
UPDATE orders 
SET status = ?, version = version + 1, ... 
WHERE id = ? AND version = ?
```

- If affected rows = 1: Success. Execute deferred side effects and publish domain events.
- If affected rows = 0: Conflict. Re-read order state and retry (up to max-retries).
- On retry, the state machine may route to a different transition (e.g., PENDING_PAY+PAY became TIMEOUT_CANCELLED+PAY).

### 4. RocketMQ Transaction Message + Delay Message

Order creation uses a two-phase commit pattern:

1. Send half message to RocketMQ (held by broker)
2. Execute local transaction in DB (insert order)
3. If local success: confirm message → becomes delay message
4. If local failure: rollback message → discarded
5. If local status unknown (producer crash): broker calls `checkLocalTransaction()` which queries DB

Delay message is delivered after timeout duration (configured in YAML).

### 5. Domain-Driven Design

- **OrderAggregate**: Contains all business rules. Methods validate preconditions, update only necessary fields, and enqueue domain events.
- **Domain Events**: Immutable, published after optimistic lock succeeds.
- **No Primitive Obsession**: OrderStatus and OrderEvent are enums, not strings.

### 6. Idempotency

Redis stores processed idempotency keys with configurable TTL. Timeout and payment consumers check idempotency first, preventing duplicate processing.

## Configuration

All hardcoded values are externalized to `application-dev.yaml` and `application-prod.yaml`.

### Key Configuration Properties (app.order.*)

```yaml
app:
  order:
    pay-timeout-minutes: 15                    # Payment timeout duration
    timeout-topic: order_timeout_topic         # RocketMQ timeout message topic
    timeout-consumer-group: order-timeout-consumer-group
    pay-callback-topic: pay_callback_topic     # Payment callback topic
    pay-callback-consumer-group: pay-callback-consumer-group
    idempotent-key-ttl-seconds: 86400          # Redis key TTL
    idempotent-key-prefix: order:idempotent:
    optimistic-lock-max-retries: 3             # Retry times on lock conflict
    payment:
      query-url: http://localhost:8080/payment/query
      query-timeout-ms: 3000
```

## REST API Endpoints

| Method | Path              | Description                           |
|--------|-------------------|---------------------------------------|
| POST   | `/orders`         | Create order                          |
| GET    | `/orders/{id}`    | Query order detail                    |
| POST   | `/orders/{id}/pay`     | Frontend polling fallback: trigger PAY |
| POST   | `/orders/{id}/cancel`  | User-initiated cancel                 |
| POST   | `/orders/{id}/use`     | Verify and use ticket                 |

All responses use unified `Response<T>` wrapper with `code`, `message`, and `data` fields.

## Event Flows

### 1. Create Order Flow

```
POST /orders (CreateOrderCmd)
  ↓
Create OrderAggregate (status=PENDING_PAY)
  ↓
Save to DB
  ↓
Send RocketMQ transaction message with delay (delay = pay-timeout-minutes)
  ↓
Return OrderDTO
```

### 2. Payment Flow

**Via Callback**:
```
RocketMQ PayCallbackConsumer receives message
  ↓
Check idempotency (Redis)
  ↓
Load order from DB (with version)
  ↓
Fire PAY event through state machine
  ↓
Optimistic lock UPDATE
  ↓
If success: execute side effects, publish domain events
If conflict: retry (may route to REFUNDING if order was cancelled)
```

**Via Frontend Polling**:
```
POST /orders/{id}/pay
  ↓
Same as callback flow above
```

### 3. Timeout Flow

```
RocketMQ OrderTimeoutConsumer receives delay message after timeout duration
  ↓
Check idempotency (Redis)
  ↓
Load order from DB (with version)
  ↓
If status != PENDING_PAY: ACK and return (already processing)
  ↓
Query payment platform to check actual status
  ↓
If payment platform returns PAID: fire PAY (compensate lost callback)
If payment platform returns UNPAID: fire TIMEOUT
  ↓
Fire event through state machine
  ↓
Optimistic lock UPDATE with retry
  ↓
If success: execute side effects, publish domain events
```

### 4. Cancel Flow

```
POST /orders/{id}/cancel
  ↓
Load order from DB
  ↓
Fire CANCEL event through state machine
  ↓
Optimistic lock UPDATE with retry
  ↓
If success: execute side effects, publish domain events
```

### 5. Use Ticket Flow

```
POST /orders/{id}/use
  ↓
Load order from DB
  ↓
Fire USE event through state machine (includes verify code validation)
  ↓
Optimistic lock UPDATE with retry
  ↓
If success: execute side effects, publish domain events
```

## Running Locally

### Prerequisites
- Java 21
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- RocketMQ 5.x

### Setup

```bash
# Build
./build.sh

# Run (dev profile)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Or via deploy script
./deploy.sh dev
```

Service starts on port 6341.

### Database Setup

```sql
CREATE DATABASE ticketing_dev;

USE ticketing_dev;

CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    venue_id VARCHAR(36) NOT NULL,
    ticket_type_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_id VARCHAR(36),
    verify_code VARCHAR(50),
    created_at DATETIME NOT NULL,
    paid_at DATETIME,
    used_at DATETIME,
    cancelled_at DATETIME,
    version INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL,
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_payment_id (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## Building and Running in Docker

```bash
# Build Docker image
docker build -t order-service:1.0.0 .

# Run container
docker run -p 6341:6341 \
  -e DB_URL=jdbc:mysql://mysql:3306/ticketing_dev \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=password \
  -e REDIS_HOST=redis \
  -e ROCKETMQ_NAME_SERVER=rocketmq:9876 \
  -e PAYMENT_QUERY_URL=http://payment-service:8080/query \
  -e SPRING_PROFILES_ACTIVE=prod \
  order-service:1.0.0
```

## Testing

The service provides extensibility for testing through:

- **TimeProvider Interface**: Inject mock time provider to control clock in tests
- **PaymentQueryService Interface**: Switch between mock and real payment service implementations
- **IdempotentChecker Interface**: Mock Redis for integration tests
- **OrderRepository Interface**: Mock database for unit tests

## Production Considerations

1. **Database Indexing**: Index on status, created_at, payment_id for query performance
2. **RocketMQ Configuration**: Ensure broker persistence and replication for reliability
3. **Redis Clustering**: Use Redis Cluster for high availability
4. **Monitoring**: Use Spring Boot Actuator endpoints for health checks
5. **Circuit Breaker**: Consider adding Resilience4j for payment service calls
6. **Logging**: All operations logged via SLF4J for troubleshooting

## Open/Closed Principle

The design follows open/closed principle for extensibility:

- **New Action**: Create new `OrderAction` implementation, register in state machine builder
- **New Transition**: Add transition in `OrderStateMachineBuilder.defineTransitions()`
- **New Payment Provider**: Implement `PaymentQueryService`, inject via Spring
- **New Config Value**: Add to `AppProperties`, no code changes needed

## License

Proprietary - Event Ticketing Platform
