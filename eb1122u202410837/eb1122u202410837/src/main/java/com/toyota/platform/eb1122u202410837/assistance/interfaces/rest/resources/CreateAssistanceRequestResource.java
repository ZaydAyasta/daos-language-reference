package com.toyota.platform.eb1122u202410837.assistance.interfaces.rest.resources;

public record CreateAssistanceRequestResource(
    Long subscriptionId,
    String issueDescription,
    String assistanceStatus,
    String requestedAt
) {
}
