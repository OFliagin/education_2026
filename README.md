# Redis Playground Service

## Цель проекта

Spring Boot сервис для ручного изучения основных сценариев использования Redis:

- cache
- TTL
- RedisTemplate
- Hash
- Set
- Sorted Set
- Rate Limiter
- Distributed Lock
- Delayed Queue
- Redis Cluster

## Общая бизнес-легенда

Есть условная система пользователей.

Сервис должен уметь:

- получать пользователей;
- кешировать пользователей;
- хранить профиль пользователя;
- отслеживать online users;
- считать активность пользователей;
- ограничивать частоту запросов;
- имитировать тяжелые операции с lock;
- создавать delayed tasks;
- показывать особенности Redis Cluster.

## Текущая реализация

Сейчас в проекте уже есть:

- Spring Boot 4.x;
- PostgreSQL;
- Redis Cluster в Docker Compose;
- Spring Data Redis;
- Spring Cache;
- базовый CRUD для пользователей;
- `@Cacheable` для чтения пользователя;
- `@CachePut` для заполнения/обновления cache при `create` и `update`.

Важно: проект сейчас использует Redis Cluster на портах `7010-7015`, а не демонстрационные `7000-7002`.

## Конфигурация

Актуальные Redis properties:

```properties
spring.data.redis.cluster.nodes=localhost:7010,localhost:7011,localhost:7012,localhost:7013,localhost:7014,localhost:7015
spring.cache.type=redis
spring.cache.cache-names=users
spring.cache.redis.time-to-live=30s
```

## Этап 1. Базовая инфраструктура

### Что сделать

Поднять проект с:

- Spring Boot 4.x
- Java 21+
- Redis Cluster в Docker
- Spring Data Redis
- Spring Cache

### Definition of Done

- приложение стартует;
- Redis Cluster поднимается через Docker;
- Spring подключается к Redis;
- можно выполнить `SET/GET`.

## Этап 2. User Cache через `@Cacheable`

### Задача

Реализовать получение пользователя по `id`.

Первый запрос идет в repository.
Повторный запрос должен идти из Redis cache.

### Нужно сделать

```java
@Cacheable(value = "users", key = "#id")
public User getUser(Long id)
```

### Что уже сделано

В проекте сейчас используется такая логика:

- `GetUserPortImpl` читает пользователя через `@Cacheable`;
- `CreateUserPortImpl` пишет пользователя в cache через `@CachePut`;
- `UpdateUserPortImpl` обновляет cache через `@CachePut`.

Это позволяет увидеть не только lazy cache на чтении, но и eager cache population на записи.

### Проверить

- в логах видно, что repository вызывается только первый раз;
- в Redis появляется ключ вида:

```text
users::1
```

- TTL ключа около `30` секунд.

### Команды Redis

Для Redis Cluster лучше использовать cluster-aware клиент:

```redis
redis-cli -c -p 7010
SCAN 0 MATCH users::*
TTL users::1
GET users::1
```

Примечание:

- сообщение `Redirected to slot ...` для Redis Cluster нормально;
- `TTL users::1 -> -1` означает, что ключ существует без expiration;
- если задан кастомный `RedisCacheConfiguration`, TTL нужно указывать в Java config явно.

## Этап 3. `RedisCacheConfiguration`

### Задача

Настроить cache serialization и TTL явно через Java config.

### Нужно сделать

- JSON serializer;
- key serializer;
- TTL;
- disable null caching.

Пример:

```java
.entryTtl(Duration.ofSeconds(30))
.disableCachingNullValues()
.serializeKeysWith(...)
.serializeValuesWith(...)
```

### Цель

Понять, что `RedisCacheConfiguration` влияет на:

- `@Cacheable`
- `@CachePut`
- `@CacheEvict`

и не влияет на:

- `RedisTemplate`

### Важное замечание по текущему состоянию проекта

Сейчас в проекте уже есть кастомный `RedisCacheConfiguration` для сериализации. Если он объявлен как бин, то `spring.cache.redis.time-to-live=30s` из properties больше не гарантирует TTL автоматически. В таком случае TTL нужно задать прямо в Java config через `.entryTtl(...)`.

## Этап 4. Manual cache через `RedisTemplate`

### Задача

Реализовать тот же User Cache, но без `@Cacheable`.

### Нужно сделать

Методы:

```java
Optional<User> findUserFromCache(Long id)
void saveUserToCache(User user)
void evictUser(Long id)
```

Ключ:

```text
user:1
```

### Цель

Сравнить:

- `@Cacheable` abstraction
- `RedisTemplate` explicit control

## Этап 5. User Profile через Redis Hash

### Задача

Хранить профиль пользователя как Redis Hash.

Ключ:

```text
user:1:profile
```

Поля:

```text
name
email
status
lastLoginAt
```

### Нужно реализовать

- save profile;
- get full profile;
- get one field;
- update one field;
- delete profile.

### Redis commands

```redis
HGETALL user:1:profile
HGET user:1:profile name
HSET user:1:profile status ACTIVE
```

## Этап 6. Online Users через Redis Set

### Задача

Отслеживать пользователей онлайн.

Ключ:

```text
online-users
```

### Нужно реализовать

- mark online;
- mark offline;
- check online;
- count online users;
- get all online users.

### Redis commands

```redis
SMEMBERS online-users
SISMEMBER online-users 1
SCARD online-users
```

## Этап 7. Leaderboard через Sorted Set

### Задача

Считать активность пользователей.

Ключ:

```text
user-activity-leaderboard
```

### Нужно реализовать

- increment user score;
- get top 10;
- get user rank;
- get user score.

### Redis commands

```redis
ZREVRANGE user-activity-leaderboard 0 9 WITHSCORES
ZSCORE user-activity-leaderboard 1
ZREVRANK user-activity-leaderboard 1
```

## Этап 8. Rate Limiter

### Задача

Ограничить количество запросов пользователя.

Например:

```text
5 requests per 60 seconds
```

Ключ:

```text
rate-limit:user:1
```

### Нужно реализовать

- при каждом запросе увеличивать counter;
- если counter первый раз создан, поставить TTL;
- если `counter > limit`, вернуть ошибку.

### Цель

Понять:

- `INCR`
- `TTL`
- `EXPIRE`
- atomic counters

## Этап 9. Distributed Lock

### Задача

Сделать endpoint:

```text
POST /reports/generate?userId=1
```

Одновременно только один report для пользователя может генерироваться.

Ключ:

```text
lock:report:user:1
```

### Первый вариант

Через `RedisTemplate`:

```java
setIfAbsent(key, value, Duration.ofSeconds(30))
```

### Потом улучшить

Через Redisson:

```java
RLock
```

## Этап 10. Delayed Queue

### Задача

После регистрации пользователя создать delayed task:

```text
send welcome email after 30 seconds
```

Использовать Redis Sorted Set.

Ключ:

```text
delayed-tasks
```

Score:

```text
timestamp when task should be executed
```

### Нужно реализовать

- add delayed task;
- scheduled worker;
- find ready tasks;
- process task;
- remove processed task.

## Этап 11. Redis Cluster demo

### Задача

Показать особенности Redis Cluster.

### Нужно реализовать

#### Обычный `SET/GET`

```text
user:1
```

#### `CROSSSLOT` example

```text
user:1:profile
user:1:settings
```

через `multiGet`.

#### Hash tag fix

```text
user:{1}:profile
user:{1}:settings
```

### Цель

Понять:

- hash slots;
- `MOVED` redirect;
- `CROSSSLOT`;
- hash tags.

## Рекомендуемый порядок реализации

1. Infrastructure
2. `@Cacheable`
3. `RedisCacheConfiguration`
4. `RedisTemplate` cache
5. Hash
6. Set
7. ZSet
8. Rate limiter
9. Distributed lock
10. Delayed queue
11. Cluster demo

## Следующий практический шаг

Ближайшая полезная задача для этого репозитория:

1. Зафиксировать TTL явно в `RedisCacheConfiguration` через `.entryTtl(Duration.ofSeconds(30))`.
2. Отключить caching `null` через `.disableCachingNullValues()`.
3. Перезаписать cache key и проверить `TTL users::1`.
4. После этого переходить к этапу 4 и сравнивать `@Cacheable` с `RedisTemplate`.
