package com.toyota.platform.eb1122u202410837.assistance.interfaces.rest.resources;

public record AssistanceRequestResource(
    Long id,
    Long subscriptionId,
    String issueDescription,
    String assistanceStatus,
    String requestedAt
) {
}
