package com.toyota.platform.eb1122u202410837.assistance.interfaces.rest.transform;

import com.toyota.platform.eb1122u202410837.assistance.domain.model.aggregates.AssistanceRequest;
import com.toyota.platform.eb1122u202410837.assistance.interfaces.rest.resources.AssistanceRequestResource;

public class AssistanceRequestResourceFromEntityAssembler {
  private AssistanceRequestResourceFromEntityAssembler() {
  }

  public static AssistanceRequestResource toResourceFromEntity(AssistanceRequest entity) {
    return new AssistanceRequestResource(
        entity.getId(),
        entity.getSubscriptionId(),
        entity.getIssueDescription(),
        entity.getAssistanceStatus().name(),
        entity.getRequestedAt().format(CreateAssistanceRequestCommandFromResourceAssembler.REQUESTED_AT_FORMATTER)
    );
  }
}
