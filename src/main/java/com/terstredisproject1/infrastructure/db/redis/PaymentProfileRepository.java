package com.terstredisproject1.infrastructure.db.redis;

import com.terstredisproject1.domain.model.PaymentPlan;
import com.terstredisproject1.domain.model.PaymentStatus;
import com.terstredisproject1.domain.model.UserPaymentProfile;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PaymentProfileRepository {
    private static final String USER_PAYMENT_PROFILE_KEY = "user:paymentProfile:";
    private static final String PAYMENT_STATUS_KEY = "paymentStatus";
    private static final String FAILED_PAYMENTS_COUNT_KEY = "failedPaymentsCount";
    private final StringRedisTemplate stringRedisTemplate;


    public void save(UserPaymentProfile userPaymentProfile) {
        stringRedisTemplate.opsForHash().putAll(
                getKey(userPaymentProfile.getUserId()),
                mapToPaymentProfileIgnoreNullValue(userPaymentProfile)
        );
    }

    public UserPaymentProfile findByUserId(Long userId) {
        Map<Object, Object> paymentProfileMap = stringRedisTemplate.opsForHash().entries(getKey(userId));
        if (paymentProfileMap.isEmpty()) {
            return null;
        }

        return mapToUserPaymentProfile(userId, paymentProfileMap);
    }

    public void update(UserPaymentProfile userPaymentProfile) {
        stringRedisTemplate.opsForHash().putAll(
                getKey(userPaymentProfile.getUserId()),
                mapToPaymentProfileIgnoreNullValue(userPaymentProfile)
        );
    }

    public void updatePaymentStatus(Long userId, PaymentStatus paymentStatus) {
        stringRedisTemplate.opsForHash().put(getKey(userId), PAYMENT_STATUS_KEY, paymentStatus.name());
    }

    public void incrementFailedPayments(Long userId) {
        stringRedisTemplate.opsForHash().increment(getKey(userId), FAILED_PAYMENTS_COUNT_KEY, 1);
    }

    public boolean exists(long userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(getKey(userId)));
    }

    public void delete(Long userId) {
        stringRedisTemplate.delete(getKey(userId));
    }


    private static UserPaymentProfile mapToUserPaymentProfile(Long userId, Map<Object, Object> paymentProfileMap) {
        return UserPaymentProfile.builder()
                .userId(userId)
                .plan(mapToPaymentPlan(paymentProfileMap))
                .currency(getString(paymentProfileMap, "currency"))
                .paymentStatus(getPaymentStatus(paymentProfileMap))
                .balanceInCents(getParsedLong(paymentProfileMap, "balanceInCents"))
                .lastPaymentAtEpochMillis(getParsedLong(paymentProfileMap, "lastPaymentAtEpochMillis"))
                .nextBillingAtEpochMillis(getParsedLong(paymentProfileMap, "nextBillingAtEpochMillis"))
                .build();
    }

    private static @NonNull PaymentStatus getPaymentStatus(Map<Object, Object> paymentProfileMap) {
        return PaymentStatus.valueOf(getString(paymentProfileMap, PAYMENT_STATUS_KEY));
    }

    private static @NonNull PaymentPlan mapToPaymentPlan(Map<Object, Object> paymentProfileMap) {
        return PaymentPlan.valueOf(getString(paymentProfileMap, "plan"));
    }

    private static long getParsedLong(Map<Object, Object> paymentProfileMap, String key) {
        return Long.parseLong(getString(paymentProfileMap, key));
    }

    private static String getString(Map<Object, Object> paymentProfileMap, String key) {
        return (String) paymentProfileMap.get(key);
    }

    private static @NonNull String getKey(Long userId) {
        return USER_PAYMENT_PROFILE_KEY + userId;
    }

    private Map<String, String> mapToPaymentProfileIgnoreNullValue(UserPaymentProfile profile) {
        Map<String, String> map = new HashMap<>();

        map.put("userId", String.valueOf(profile.getUserId()));

        if (profile.getPlan() != null) {
            map.put("plan", profile.getPlan().name());
        }
        if (profile.getCurrency() != null) {
            map.put("currency", profile.getCurrency());
        }
        if (profile.getPaymentStatus() != null) {
            map.put(PAYMENT_STATUS_KEY, profile.getPaymentStatus().name());
        }
        if (profile.getBalanceInCents() != null) {
            map.put("balanceInCents", String.valueOf(profile.getBalanceInCents()));
        }
        if (profile.getLastPaymentAtEpochMillis() != null) {
            map.put("lastPaymentAtEpochMillis", String.valueOf(profile.getLastPaymentAtEpochMillis()));
        }
        if (profile.getNextBillingAtEpochMillis() != null) {
            map.put("nextBillingAtEpochMillis", String.valueOf(profile.getNextBillingAtEpochMillis()));
        }
        if (profile.getFailedPaymentsCount() != null) {
            map.put(FAILED_PAYMENTS_COUNT_KEY, String.valueOf(profile.getFailedPaymentsCount()));
        }

        return map;
    }

}
