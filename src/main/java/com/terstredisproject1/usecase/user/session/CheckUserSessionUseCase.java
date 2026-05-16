package com.terstredisproject1.usecase.user.session;

import com.terstredisproject1.usecase.user.port.CheckUserSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckUserSessionUseCase {
    private final CheckUserSessionPort checkUserSessionPort;


    public boolean isUserOnline(long userId) {
        return checkUserSessionPort.isUserOnline(userId);
    }
}
