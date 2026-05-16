package com.terstredisproject1.usecase.user.port;

import com.terstredisproject1.domain.model.UserPaymentProfile;

public interface GetPaymentProfilePort {

    UserPaymentProfile execute(long userId);
}
