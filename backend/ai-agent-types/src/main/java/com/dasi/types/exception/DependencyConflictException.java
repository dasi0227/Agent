package com.dasi.types.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class DependencyConflictException extends RuntimeException {

    private final List<String> dependents;

    public DependencyConflictException(String message, List<String> dependents) {
        super(message);
        this.dependents = dependents;
    }

}
