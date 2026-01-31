package com.dasi.domain.admin.model.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCommand {

    private Long id;

    private String clientId;

    private String clientType;

    private String modelId;

    private String modelName;

    private String clientName;

    private String clientDesc;

    private Integer clientStatus;
}
