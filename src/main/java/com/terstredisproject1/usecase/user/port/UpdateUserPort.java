package com.terstredisproject1.usecase.user.port;

import com.terstredisproject1.domain.model.User;

public interface UpdateUserPort {
    void updateUser(long id, User user);
}
