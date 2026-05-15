package com.terstredisproject1.infrastructure.db.pg;

import com.terstredisproject1.domain.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);
}
