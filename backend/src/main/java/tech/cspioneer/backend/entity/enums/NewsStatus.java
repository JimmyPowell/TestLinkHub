package tech.cspioneer.backend.entity.enums;

import lombok.Getter;

@Getter
public enum NewsStatus {
    pending,
    published,
    archived;
    private String message;
}
