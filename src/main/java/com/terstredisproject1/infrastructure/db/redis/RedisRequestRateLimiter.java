package com.terstredisproject1.infrastructure.db.redis;

import com.terstredisproject1.domain.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Redis-backed request rate limiter for AI message endpoints.
 *
 * <p>This is a learning project, so the class intentionally contains several
 * independent implementations of the <em>same</em> idea — limiting how many
 * requests a user may send. They differ along two axes:
 * <ul>
 *     <li><b>Algorithm</b> — Fixed Window, Sliding Window or Token Bucket.</li>
 *     <li><b>Atomicity</b> — a server-side Lua script (atomic, race-free,
 *         safe across multiple application instances) vs. a multi-call Java
 *         implementation (not atomic, prone to race conditions under
 *         concurrent requests).</li>
 * </ul>
 *
 * <p>Every {@code check*} method either returns normally when the request is
 * allowed, or throws {@link TooManyRequestsException} when the user has hit
 * their limit.
 *
 * <p>Keys use two prefixes: {@value #COUNTER_PREFIX} for window-counter
 * implementations and {@value #REQUEST_QUOTA_PREFIX} for the token-bucket state.
 */
@Repository
@RequiredArgsConstructor
public class RedisRequestRateLimiter {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String COUNTER_PREFIX = "rate-limit:ai-message:";
    @Value("${ai.task.sent.limit:10}")
    private int sentLimit;
    @Value("${ai.task.sent.window.seconds:60}")
    private long windowSeconds;

    @Value("${ai.task.rate-limit.token-bucket.capacity:10}")
    private int tokenBucketCapacity;
    @Value("${ai.task.rate-limit.token-bucket.refill-rate-per-second:1}")
    private int tokenBucketRefillRatePerSecond;
    @Value("${ai.task.rate-limit.token-bucket.ttl-seconds:120}")
    private int tokenBucketTtlSeconds;
    private static final String REQUEST_QUOTA_PREFIX = "request-quota:";
    private static final long ALLOWED = 1L;
    private static final long REJECTED = 0L;

    private static final DefaultRedisScript<Long> TOKEN_BUCKET_RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>("""
                local key = KEYS[1]

                local now = tonumber(ARGV[1])
                local capacity = tonumber(ARGV[2])
                local refillRatePerSecond = tonumber(ARGV[3])
                local requestedTokens = tonumber(ARGV[4])
                local ttlSeconds = tonumber(ARGV[5])

                local currentTokens = redis.call('HGET', key, 'tokens')
                local lastRefillTime = redis.call('HGET', key, 'lastRefillTime')

                if currentTokens == false or lastRefillTime == false then
                    currentTokens = capacity
                    lastRefillTime = now
                else
                    currentTokens = tonumber(currentTokens)
                    lastRefillTime = tonumber(lastRefillTime)
                end

                local elapsedMillis = now - lastRefillTime
                local tokensToAdd = math.floor(elapsedMillis / 1000) * refillRatePerSecond

                local availableTokens = math.min(capacity, currentTokens + tokensToAdd)

                if availableTokens < requestedTokens then
                    redis.call('HSET', key,
                        'tokens', availableTokens,
                        'lastRefillTime', lastRefillTime
                    )
                    redis.call('EXPIRE', key, ttlSeconds)
                    return 0
                end

                local remainingTokens = availableTokens - requestedTokens

                local newLastRefillTime = lastRefillTime
                if tokensToAdd > 0 then
                    newLastRefillTime = lastRefillTime + math.floor(elapsedMillis / 1000) * 1000
                end

                redis.call('HSET', key,
                    'tokens', remainingTokens,
                    'lastRefillTime', newLastRefillTime
                )

                redis.call('EXPIRE', key, ttlSeconds)

                return 1
                """, Long.class);

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>("""
                    local count = redis.call('INCR', KEYS[1])
                    if count == 1 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end
                    return count
                    """, Long.class);


    private static final DefaultRedisScript<Long> SLIDING_WINDOW_RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>("""
                local key = KEYS[1]
                local now = tonumber(ARGV[1])
                local window = tonumber(ARGV[2])
                local limit = tonumber(ARGV[3])
                local member = ARGV[4]
                local ttl = tonumber(ARGV[5])

                redis.call('ZREMRANGEBYSCORE', key, 0, now - window)

                local count = redis.call('ZCARD', key)

                if count >= limit then
                    return 0
                end

                redis.call('ZADD', key, now, member)
                redis.call('EXPIRE', key, ttl)

                return 1
                """, Long.class);



    /**
     * Token Bucket rate limit — <b>atomic</b> implementation.
     *
     * <p>The whole algorithm runs inside Redis as a single Lua script
     * ({@link #TOKEN_BUCKET_RATE_LIMIT_SCRIPT}): token refill calculation,
     * limit validation, token consumption, state update and TTL refresh.
     * Because everything happens in one server-side operation there are no
     * race conditions, and the limiter is safe to use across multiple
     * application instances.
     *
     * <p>Bucket state is kept in a Redis Hash under
     * {@value #REQUEST_QUOTA_PREFIX}{@code <userId>} with fields {@code tokens}
     * and {@code lastRefillTime}. Each call consumes one token.
     *
     * @param userId id of the user whose quota is checked
     * @throws TooManyRequestsException if the bucket has no tokens left
     * @throws IllegalStateException    if the Lua script returns no result
     */
    public void checkTokenBucketLimitLua(Long userId) {
        String key = getRequestQuotaKey(userId);

        long now = System.currentTimeMillis();
        long requestedTokens = 1L; // one request costs one token

        // Hand the whole algorithm to Redis: the script reads the bucket, refills it by
        // elapsed time, checks/consumes a token, writes the new state and refreshes the
        // TTL — all atomically. It returns 1 (allowed) or 0 (rejected).
        Long allowed = stringRedisTemplate.execute(
                TOKEN_BUCKET_RATE_LIMIT_SCRIPT,
                List.of(key),
                String.valueOf(now),
                String.valueOf(tokenBucketCapacity),
                String.valueOf(tokenBucketRefillRatePerSecond),
                String.valueOf(requestedTokens),
                String.valueOf(tokenBucketTtlSeconds)
        );

        // null means the script did not run (e.g. connection issue) — not a limit decision.
        if (allowed == null) {
            throw new IllegalStateException("Failed to execute token bucket rate limit script");
        }

        // 0 -> no token available: reject this request.
        if (allowed == REJECTED) {
            throw new TooManyRequestsException(
                    "User " + userId
                            + " has reached the token bucket request limit. "
                            + "Capacity=" + tokenBucketCapacity
                            + ", refillRatePerSecond=" + tokenBucketRefillRatePerSecond
            );
        }
    }

    /**
     * Token Bucket rate limit — <b>non-atomic</b> implementation.
     *
     * <p>Same algorithm as {@link #checkTokenBucketLimitLua(Long)}, but the
     * read of the bucket, the refill/validation and the write-back are done as
     * separate Redis calls from Java. This is <b>not thread-safe</b>: under
     * concurrent requests two callers can read the same token count and both
     * be allowed, exceeding the limit.
     *
     * <p>Bucket state lives in a Redis Hash under
     * {@value #REQUEST_QUOTA_PREFIX}{@code <userId>}; tokens are refilled based
     * on elapsed time and one token is consumed per call.
     *
     * <p>Kept only for learning/comparison — prefer the atomic Lua variant in
     * production.
     *
     * @param userId id of the user whose quota is checked
     * @throws TooManyRequestsException if the bucket has no tokens left
     */
    public void checkTokenBucketLimit(Long userId) {
        String key = getRequestQuotaKey(userId);

        // Defaults for a brand-new bucket: start full at capacity, "now" as the timestamp.
        long availableTokens = tokenBucketCapacity;
        long currentTime = System.currentTimeMillis();

        // Does the bucket already exist in Redis? If not, we treat it as a full bucket.
        final boolean empty = stringRedisTemplate.opsForHash().entries(key).isEmpty();
        long tokensToAdd = 0;
        if (!empty) {
            // 1. Read the persisted state: how many tokens were left and when we last refilled.
            long currentTokens = Long.parseLong(stringRedisTemplate.opsForHash().get(key, "tokens").toString());
            long lastRefillTime = Long.parseLong(stringRedisTemplate.opsForHash().get(key, "lastRefillTime").toString());

            // 2. Refill: add one token per whole second elapsed since the last refill,
            //    capped at the bucket capacity (tokens don't accumulate past full).
            long refillInterval = currentTime - lastRefillTime;
            tokensToAdd = refillInterval / 1000 * tokenBucketRefillRatePerSecond;
            availableTokens = Math.min(tokenBucketCapacity, currentTokens + tokensToAdd);

            // 3. Reject if even after refill the bucket is empty (no token to spend).
            if (availableTokens < 1) {
                throw new TooManyRequestsException("User " + userId + " has reached the limit of " + tokenBucketCapacity + " messages per minute");
            }
        }

        // 4. Consume one token for this request and persist the new state back to Redis.
        stringRedisTemplate.opsForHash().put(key, "tokens", availableTokens - 1);
        stringRedisTemplate.opsForHash().put(key, "lastRefillTime", currentTime);

        // 5. Set the TTL only when no refill happened (tokensToAdd == 0). NOTE: this is a
        //    learning-grade heuristic — the bucket's expiry is not refreshed on every call,
        //    so an active-but-not-refilling bucket can expire. The atomic Lua variant
        //    (checkTokenBucketLimitLua) refreshes the TTL on every request instead.
        if (tokensToAdd == 0) {
            stringRedisTemplate.opsForHash().expire(key, Duration.ofSeconds(tokenBucketTtlSeconds), Set.of("tokens", "lastRefillTime"));
        }
    }


    /**
     * Sliding Window rate limit — <b>atomic</b> implementation.
     *
     * <p>Backed by a Redis Sorted Set where each request is a member scored by
     * its timestamp. The Lua script
     * ({@link #SLIDING_WINDOW_RATE_LIMIT_SCRIPT}) atomically evicts entries
     * older than the window, counts what remains, rejects when the limit is
     * reached, otherwise records the current request and refreshes the TTL.
     *
     * <p>Unlike a fixed window this smoothly counts requests over the trailing
     * {@code windowSeconds}, avoiding burst spikes at window boundaries. The
     * key is {@value #COUNTER_PREFIX}{@code <userId>} and the member is a
     * unique {@code <timestamp>:<uuid>} value.
     *
     * @param userId id of the user whose requests are counted
     * @throws TooManyRequestsException if {@code sentLimit} requests already
     *                                  occurred within the window
     * @throws IllegalStateException    if the Lua script returns no result
     */
    public void checkSlidingWindowLimitLua(Long userId) {
        final String key = getKey(userId);

        long now = System.currentTimeMillis();
        long windowMillis = Duration.ofSeconds(windowSeconds).toMillis(); // window length in ms (the ZSet score unit)
        long ttlSeconds = windowSeconds * 2; // keep the key a bit past the window so idle users get cleaned up
        String member = now + ":" + UUID.randomUUID(); // unique member so identical-timestamp requests don't collide

        // The script does it all atomically: drop entries older than the window, count the
        // rest, reject if >= limit, otherwise add this request and refresh the TTL.
        // Returns 1 (allowed) or 0 (rejected).
        Long allowed = stringRedisTemplate.execute(
                SLIDING_WINDOW_RATE_LIMIT_SCRIPT,
                List.of(key),
                String.valueOf(now),
                String.valueOf(windowMillis),
                String.valueOf(sentLimit),
                member,
                String.valueOf(ttlSeconds)
        );

        // null means the script did not run — not a limit decision.
        if (allowed == null) {
            throw new IllegalStateException("Failed to execute sliding window rate limit script");
        }

        // 0 -> limit already reached within the window: reject.
        if (allowed == REJECTED) {
            throw new TooManyRequestsException(
                    "User " + userId + " has reached the limit of "
                            + sentLimit + " messages per "
                            + windowSeconds + " seconds"
            );
        }
    }

    /**
     * Sliding Window rate limit — <b>non-atomic</b> implementation.
     *
     * <p>Same Sorted Set approach as {@link #checkSlidingWindowLimitLua(Long)},
     * but the steps (evict old entries, count, check, add the new entry,
     * refresh TTL) run as separate Redis calls from Java. Between the count and
     * the add another concurrent request can slip in, so under load the limit
     * may be exceeded.
     *
     * <p>Key is {@value #COUNTER_PREFIX}{@code <userId>}. Kept for
     * learning/comparison — prefer the atomic Lua variant in production.
     *
     * @param userId id of the user whose requests are counted
     * @throws TooManyRequestsException if {@code sentLimit} requests already
     *                                  occurred within the window
     * @throws IllegalStateException    if the counter cannot be read
     */
    public void checkSlidingWindowLimit(Long userId) {
        final String key = getKey(userId);

        long now = System.currentTimeMillis();
        long windowStart = now - Duration.ofSeconds(windowSeconds).toMillis(); // oldest timestamp still inside the window

        // 1. Evict requests that fell out of the trailing window (score from 0 to windowStart).
        stringRedisTemplate.opsForZSet().removeRangeByScore(
                key,
                0,
                windowStart

        );

        // 2. Count what remains — that's how many requests happened within the window.
        Long currentCount = stringRedisTemplate.opsForZSet().zCard(key);
        if (currentCount == null) {
            throw new IllegalStateException("Failed to read sliding window rate limit counter");
        }

        // 3. Reject if the limit is already reached. NOTE: steps 2-4 are separate Redis
        //    calls, so two concurrent requests can both read the same count here and both
        //    pass — that's the race the atomic Lua variant avoids.
        if (currentCount >= sentLimit) {
            throw new TooManyRequestsException(
                    "User " + userId + " has reached the limit of "
                            + sentLimit + " messages per "
                            + windowSeconds + " seconds"
            );
        }

        // 4. Record the current request as a unique member scored by its timestamp.
        String requestId = now + ":" + UUID.randomUUID();

        stringRedisTemplate.opsForZSet().add(
                key,
                requestId,
                now
        );

        // 5. Refresh the TTL so the key (and its memory) is reclaimed once the user goes idle.
        stringRedisTemplate.expire(
                key,
                Duration.ofSeconds(windowSeconds * 2)
        );
    }

    /**
     * Fixed Window rate limit — <b>atomic</b> implementation.
     *
     * <p>A single counter per user-window. The Lua script
     * ({@link #RATE_LIMIT_SCRIPT}) atomically {@code INCR}s the counter and, on
     * the first hit, sets its TTL to the window length, so the increment and
     * the expiry can never race apart. When the counter exceeds
     * {@code sentLimit} the request is rejected.
     *
     * <p>Simplest algorithm, but it allows bursts at window edges (up to
     * {@code 2 * sentLimit} around a boundary). Key is
     * {@value #COUNTER_PREFIX}{@code <userId>}.
     *
     * @param userId id of the user whose requests are counted
     * @throws TooManyRequestsException if the counter exceeds {@code sentLimit}
     * @throws IllegalStateException    if the Lua script returns no result
     */
    public void checkFixedWindowLuaImpl(long userId) {
        final String key = getKey(userId);

        // Atomically INCR the counter and, only on the first hit, set its TTL to the window
        // length. Returns the new counter value for this window.
        Long count = stringRedisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of(key),
                windowSeconds
        );

        // null means the script did not run — not a limit decision.
        if (count == null) {
            throw new IllegalStateException("Failed to increment rate limit counter");
        }

        // Past the allowance for this window -> reject.
        if (count > sentLimit) {
            throw new TooManyRequestsException("User " + userId + " has reached the limit of " + sentLimit + " messages per minute");
        }
    }

    /**
     * Fixed Window rate limit — implemented with Spring Redis calls instead of
     * a Lua script.
     *
     * <p>Same fixed-window idea as {@link #checkFixedWindowLuaImpl(long)}: an
     * atomic {@code INCR} via {@code opsForValue().increment()}, then on the
     * first hit (value {@code == 1}) a separate {@code EXPIRE} sets a one-minute
     * TTL. The increment itself is atomic, but the TTL is set in a second call,
     * so a crash between the two could leave the key without expiry — the Lua
     * variant avoids that gap.
     *
     * <p>Key is {@value #COUNTER_PREFIX}{@code <userId>}.
     *
     * @param userId id of the user whose requests are counted
     * @throws TooManyRequestsException if the counter exceeds {@code sentLimit}
     * @throws IllegalStateException    if the counter cannot be incremented
     */
    public void checkFixedWindowSpringImplementation(long userId) {
        final String key = getKey(userId);

        // 1. Atomically increment the per-window counter (creates it at 1 on first request).
        final Long increment = stringRedisTemplate.opsForValue().increment(key);
        if (increment == null) {
            throw new IllegalStateException("Failed to increment rate limit counter");
        }

        // 2. On the very first request of the window, set the 1-minute TTL. This is a
        //    second, separate call (unlike the Lua variant), so a crash right after the
        //    INCR could leave the counter without an expiry and block the user forever.
        if (increment == 1) {
            stringRedisTemplate.expire(key, Duration.ofMinutes(1));
        }

        // 3. Past the allowance for this window -> reject.
        if (increment > sentLimit) {
            throw new TooManyRequestsException("User " + userId + " has reached the limit of " + sentLimit + " messages per minute");
        }
    }


    private String getKey(long userId) {
        return COUNTER_PREFIX + userId;
    }

    private String getRequestQuotaKey(long userId) {
        return REQUEST_QUOTA_PREFIX + userId;
    }
}
