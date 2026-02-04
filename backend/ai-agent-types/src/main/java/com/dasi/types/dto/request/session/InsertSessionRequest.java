package com.dasi.types.dto.request.session;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsertSessionRequest {

    @NotBlank
    private String sessionTitle;

    @NotBlank
    private String sessionType;

}
