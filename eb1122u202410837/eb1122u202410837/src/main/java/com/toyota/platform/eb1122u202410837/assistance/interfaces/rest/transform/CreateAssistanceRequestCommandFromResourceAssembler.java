package com.toyota.platform.eb1122u202410837.assistance.interfaces.rest.transform;

import com.toyota.platform.eb1122u202410837.assistance.domain.model.commands.CreateAssistanceRequestCommand;
import com.toyota.platform.eb1122u202410837.assistance.domain.model.valueobjects.AssistanceStatus;
import com.toyota.platform.eb1122u202410837.assistance.interfaces.rest.resources.CreateAssistanceRequestResource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateAssistanceRequestCommandFromResourceAssembler {
  public static final DateTimeFormatter REQUESTED_AT_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private CreateAssistanceRequestCommandFromResourceAssembler() {
  }

  public static CreateAssistanceRequestCommand toCommandFromResource(CreateAssistanceRequestResource resource) {
    return new CreateAssistanceRequestCommand(
        resource.subscriptionId(),
        resource.issueDescription(),
        AssistanceStatus.valueOf(resource.assistanceStatus()),
        LocalDateTime.parse(resource.requestedAt(), REQUESTED_AT_FORMATTER)
    );
  }
}
