package com.toyota.platform.eb1122u202410837.assistance.domain.model.events;

import java.time.LocalDateTime;

public record AssistanceRequestCreatedEvent(
    Long subscriptionId,
    Long assistanceRequestId,
    LocalDateTime requestedAt
) {
}
