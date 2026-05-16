package com.terstredisproject1.usecase.user.session;

import com.terstredisproject1.usecase.user.port.RefreshUserSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshUserSessionUseCase {
    private final RefreshUserSessionPort refreshUserSessionPort;

    public void execute(long userId) {
        refreshUserSessionPort.refreshSession(userId);
    }
}
