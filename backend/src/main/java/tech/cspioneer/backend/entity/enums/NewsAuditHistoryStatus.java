package tech.cspioneer.backend.entity.enums;

import lombok.Getter;

@Getter
public enum NewsAuditHistoryStatus {
    approved,
    rejected;
    private String message;
}
