package com.terstredisproject1.usecase.agent;

import com.terstredisproject1.domain.model.agent.DilayMessage;
import com.terstredisproject1.usecase.agent.port.ProcessDilayMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetDilayMessageUseCase {
    private final ProcessDilayMessagePort processDilayMessagePort;


    public List<DilayMessage> execute() {
        return processDilayMessagePort.getDilayMessageTask();
    }
}
