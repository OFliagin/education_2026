package com.terstredisproject1.usecase.agent;

import com.terstredisproject1.domain.model.agent.DilayMessage;
import com.terstredisproject1.usecase.agent.port.DeleteDilayMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteDilayMessageUseCase {
    private final DeleteDilayMessagePort deleteDilayMessagePort;

    public void delete(DilayMessage dilayMessage) {
        deleteDilayMessagePort.getDilayMessageTask(dilayMessage);
    }
}
