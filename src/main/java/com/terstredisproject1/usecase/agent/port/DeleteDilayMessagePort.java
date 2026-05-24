package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.agent.DilayMessage;

public interface DeleteDilayMessagePort {
    void getDilayMessageTask(DilayMessage dilayMessage);
}
