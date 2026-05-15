package com.terstredisproject1.usecase.user.port;

import com.terstredisproject1.domain.model.User;

public interface UpdateUserPort {
    User updateUser(long id, User user);
}
