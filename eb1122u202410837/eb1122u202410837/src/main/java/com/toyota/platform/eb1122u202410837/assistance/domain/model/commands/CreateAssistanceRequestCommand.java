package com.toyota.platform.eb1122u202410837.assistance.domain.model.commands;

import com.toyota.platform.eb1122u202410837.assistance.domain.model.valueobjects.AssistanceStatus;
import java.time.LocalDateTime;

public record CreateAssistanceRequestCommand(
    Long subscriptionId,
    String issueDescription,
    AssistanceStatus assistanceStatus,
    LocalDateTime requestedAt
) {
}
