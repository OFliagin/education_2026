package com.terstredisproject1.usecase.agent.port;

import com.terstredisproject1.domain.model.agent.DilayMessage;

import java.util.List;

public interface ProcessDilayMessagePort {

    List<DilayMessage> getDilayMessageTask();
}
