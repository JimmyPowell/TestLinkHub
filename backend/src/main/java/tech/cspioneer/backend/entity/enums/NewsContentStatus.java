package tech.cspioneer.backend.entity.enums;

import lombok.Getter;

@Getter
public enum NewsContentStatus {
    pending,
    published,
    rejected,
    archived;
    private String message;
}
