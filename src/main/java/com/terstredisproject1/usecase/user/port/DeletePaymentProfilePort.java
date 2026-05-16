package com.terstredisproject1.usecase.user.port;

import com.terstredisproject1.domain.model.UserPaymentProfile;

public interface DeletePaymentProfilePort {

    void execute(long userId);
}
