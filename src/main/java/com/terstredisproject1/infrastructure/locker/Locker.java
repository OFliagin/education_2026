package com.terstredisproject1.infrastructure.locker;

public interface Locker {

    boolean lock(String lockerId);
    void unlock(String lockerId);
}
