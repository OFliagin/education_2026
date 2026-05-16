package com.terstredisproject1.usecase.user.port;

import com.terstredisproject1.domain.model.UserPaymentProfile;

import java.util.Optional;

public interface GetPaymentProfilePort {

    Optional<UserPaymentProfile> execute(long userId);
}
