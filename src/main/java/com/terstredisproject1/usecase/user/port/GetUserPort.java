package com.terstredisproject1.usecase.user.port;

import com.terstredisproject1.domain.model.User;

public interface GetUserPort {
    User getUser(long userId);
}
