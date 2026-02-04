package com.dasi.types.dto.request.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSessionRequest {

    @NotNull
    private Long id;

    @NotBlank
    private String sessionId;

    @NotBlank
    private String sessionTitle;

}
