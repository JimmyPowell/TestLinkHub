package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MarkNotificationsAsReadRequest {
    private List<String> notificationUuids;
}
