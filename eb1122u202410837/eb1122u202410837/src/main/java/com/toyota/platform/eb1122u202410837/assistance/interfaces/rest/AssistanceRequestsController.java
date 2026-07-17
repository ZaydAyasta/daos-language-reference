package com.toyota.platform.eb1122u202410837.assistance.interfaces.rest;

import com.toyota.platform.eb1122u202410837.assistance.domain.services.AssistanceRequestCommandService;
import com.toyota.platform.eb1122u202410837.assistance.interfaces.rest.resources.AssistanceRequestResource;
import com.toyota.platform.eb1122u202410837.assistance.interfaces.rest.resources.CreateAssistanceRequestResource;
import com.toyota.platform.eb1122u202410837.assistance.interfaces.rest.transform.AssistanceRequestResourceFromEntityAssembler;
import com.toyota.platform.eb1122u202410837.assistance.interfaces.rest.transform.CreateAssistanceRequestCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/assistance-requests", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Assistance Requests", description = "Assistance request management endpoints")
public class AssistanceRequestsController {
  private final AssistanceRequestCommandService assistanceRequestCommandService;

  public AssistanceRequestsController(AssistanceRequestCommandService assistanceRequestCommandService) {
    this.assistanceRequestCommandService = assistanceRequestCommandService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create an assistance request")
  public ResponseEntity<AssistanceRequestResource> createAssistanceRequest(
      @RequestBody CreateAssistanceRequestResource resource) {
    var command = CreateAssistanceRequestCommandFromResourceAssembler.toCommandFromResource(resource);
    var assistanceRequest = assistanceRequestCommandService.handle(command).orElseThrow();
    var assistanceRequestResource =
        AssistanceRequestResourceFromEntityAssembler.toResourceFromEntity(assistanceRequest);
    return new ResponseEntity<>(assistanceRequestResource, HttpStatus.CREATED);
  }
}
