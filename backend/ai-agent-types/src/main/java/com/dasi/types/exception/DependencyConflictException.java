package com.dasi.types.exception;

import java.util.List;

/**
 * 依赖冲突异常：禁用/删除/更新标识时发现有业务引用。
 */
public class DependencyConflictException extends RuntimeException {

    private final List<String> dependents;

    public DependencyConflictException(String message, List<String> dependents) {
        super(message);
        this.dependents = dependents;
    }

    public List<String> getDependents() {
        return dependents;
    }
}
