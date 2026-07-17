package com.toyota.platform.eb1122u202410837.assistance.domain.services;

import com.toyota.platform.eb1122u202410837.assistance.domain.model.aggregates.AssistanceRequest;
import com.toyota.platform.eb1122u202410837.assistance.domain.model.commands.CreateAssistanceRequestCommand;
import java.util.Optional;

public interface AssistanceRequestCommandService {
  Optional<AssistanceRequest> handle(CreateAssistanceRequestCommand command);
}
