package com.terstredisproject1.infrastructure.db;

import com.terstredisproject1.domain.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
