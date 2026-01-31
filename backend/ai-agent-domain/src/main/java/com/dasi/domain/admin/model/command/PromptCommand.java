package com.dasi.domain.admin.model.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptCommand {

    private Long id;

    private String promptId;

    private String promptName;

    private String promptContent;

    private String promptDesc;

    private Integer promptStatus;
}
