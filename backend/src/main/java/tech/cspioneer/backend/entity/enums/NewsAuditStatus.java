package tech.cspioneer.backend.entity.enums;

import lombok.Getter;

@Getter
public enum NewsAuditStatus {
    rejected,
    approved;
    private String message;
}
