package com.terstredisproject1.infrastructure.locker;

import java.util.UUID;

public interface Locker {

    boolean lock(String lockerId, UUID lockUuid);
    void unlock(String lockerId, UUID lockUuid);
}
