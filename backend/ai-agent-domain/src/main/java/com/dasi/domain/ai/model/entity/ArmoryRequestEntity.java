package com.dasi.domain.ai.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArmoryRequestEntity {

    private String armoryType;

    private Set<String> armoryIdSet;

}
