package com.terstredisproject1.usecase.user.port;

import com.terstredisproject1.domain.model.User;

public interface CreateUserPort {
    User createUser(User user);
}
