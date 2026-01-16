package com.dasi.domain.agent.service.execute;

import com.dasi.domain.agent.model.entity.ExecuteCommandEntity;

public interface IExecuteStrategy {

    void execute(ExecuteCommandEntity executeCommandEntity) throws Exception;

}
