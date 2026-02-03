package com.dasi.types.dto.request.admin.manage;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptManageRequest {

    private Long id;

    @NotBlank
    private String promptId;

    @NotBlank
    private String promptName;

    @NotBlank
    private String promptContent;

    private String promptDesc;

}
