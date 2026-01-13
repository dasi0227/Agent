package com.dasi.domain.agent.model.entity;

import lombok.Data;

import java.util.List;

@Data
public class ArmoryCommandEntity {

    private String commandType;

    private List<String> commandIdList;

}
