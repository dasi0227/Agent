package com.dasi.types.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArmoryRequest {
    
    @NotBlank
    private String armoryType;

    @NotBlank
    private String armoryId;

}
