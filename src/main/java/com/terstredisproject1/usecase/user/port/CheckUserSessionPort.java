package com.terstredisproject1.usecase.user.port;

public interface CheckUserSessionPort {

    boolean isUserOnline(long userId);
}
