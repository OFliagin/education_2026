# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start infrastructure (Redis Cluster + PostgreSQL)
docker compose up -d

# Build
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=UserCachePortsTest

# Run a single test method
./mvnw test -Dtest=UserCachePortsTest#create_user_populates_cache_immediately
```

## Infrastructure

The app requires both services from `compose.yaml`:
- **PostgreSQL** on port `5432` — primary user storage, migrated via Flyway (`src/main/resources/db/migration/`)
- **Redis Cluster** on ports `7010–7015` — 6-node cluster (3 primaries + 3 replicas), all in one container

`spring.docker.compose.lifecycle-management=start-only` means Spring Boot will start Docker Compose automatically on app startup but won't stop it on shutdown.

To inspect Redis in cluster mode:
```bash
redis-cli -c -p 7010
SCAN 0 MATCH users::*
```

## Architecture

The project follows **Hexagonal Architecture (Ports and Adapters)**:

```
adapter/controller/       → REST layer (controllers, request/response DTOs, validators)
usecase/user/             → Application layer (use cases + port interfaces)
  port/                   → Port interfaces (e.g. GetUserPort, LoginUserPort)
  session/                → Session use cases
  profile/                → Payment profile use cases
infrastructure/adapter/port/ → Port implementations (wire use cases to repos)
infrastructure/db/pg/     → JPA repositories (PostgreSQL)
infrastructure/db/redis/  → Redis repositories (StringRedisTemplate)
domain/model/             → Domain entities
domain/config/            → Spring configuration (CacheConfig)
```

**Dependency direction:** controllers → use cases → port interfaces ← port implementations → repositories

Use cases only depend on port interfaces. Implementations in `infrastructure/adapter/port/` inject both the JPA/Redis repositories and implement the port interface.

## Redis Data Patterns

Two distinct Redis interaction patterns coexist:

1. **Spring Cache abstraction** (`@Cacheable`, `@CachePut`) — used for `users` cache in `GetUserPortImpl`, `CreateUserPortImpl`, `UpdateUserPortImpl`. Cache is configured via `CacheConfig` with JSON serialization and TTL from `cache.redis.ttl.sec` (default 30s).

2. **Manual `StringRedisTemplate`** — used for session storage (`SessionUserRepository`, key `session:user:{id}`) and payment profiles (`PaymentProfileRepository`, key `user:paymentProfile:{id}` stored as a Redis Hash).

`RedisCacheConfiguration` bean affects only Spring Cache (`@Cacheable` etc.) — not `RedisTemplate` operations.

## Key Configuration Properties

| Property | Default | Purpose |
|---|---|---|
| `cache.redis.ttl.sec` | 30 | TTL for `users` Spring Cache entries |
| `user.session.ttl.min` | 3 | TTL for user session keys |
| `spring.data.redis.cluster.nodes` | `localhost:7010–7015` | Redis Cluster node list |

## Agent / Token Usage Flow

### Endpoints
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/agent/message` | Send a message; consumes tokens |
| `GET` | `/api/agent/user/{userId}/token-usage` | Read current token usage |

### Token cost formula
`ceil(message.length / 50) * 10` tokens per request (in `GetMessagePortImpl`).

### Token storage
`RedisTokenRepository` stores per-user daily usage under key `ai:token-usage:{userId}:{yyyyMMdd}` (manual `StringRedisTemplate`, not Spring Cache). TTL is set to end of UTC day on first write.

### Payment plans
`PaymentPlan` enum defines per-plan token budgets:

| Plan | Available tokens |
|---|---|
| `BASIC` | 100 |
| `PRO` | 2 000 |
| `PREMIUM` | 5 000 |

### Limit enforcement
`TokenUsageLimiter.isTokenLimitExceeded(userId, tokenUsage)` blocks a request when either:
- tokens already used `>=` plan limit (hard cap), or
- tokens used `+` cost of this request `>` plan limit (pre-flight check).

A missing payment profile throws `IllegalStateException` (fail-fast; the profile must exist before messaging). Exceeding the limit throws `TokenLimitExceeded`, handled by `AgentController.handleTokenLimitExceeded` with **HTTP 429**.

## Testing Approach

Cache behavior tests (`UserCachePortsTest`) use `@SpringJUnitConfig` with an in-memory `ConcurrentMapCacheManager` instead of a real Redis connection — this tests the `@Cacheable`/`@CachePut` annotations without infrastructure. The port implementation beans are wired directly in a `@Configuration` inner class using mocked JPA repositories.

Integration tests would use Testcontainers (wiring already present in `TestcontainersConfiguration`).
