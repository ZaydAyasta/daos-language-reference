package com.toyota.platform.eb1122u202410837.subscriptions.interfaces.rest;

import com.toyota.platform.eb1122u202410837.subscriptions.domain.services.VehicleSubscriptionCommandService;
import com.toyota.platform.eb1122u202410837.subscriptions.interfaces.rest.resources.CreateVehicleSubscriptionResource;
import com.toyota.platform.eb1122u202410837.subscriptions.interfaces.rest.resources.VehicleSubscriptionResource;
import com.toyota.platform.eb1122u202410837.subscriptions.interfaces.rest.transform.CreateVehicleSubscriptionCommandFromResourceAssembler;
import com.toyota.platform.eb1122u202410837.subscriptions.interfaces.rest.transform.VehicleSubscriptionResourceFromEntityAssembler;
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
@RequestMapping(value = "/api/v1/vehicle-subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Vehicle Subscriptions", description = "Vehicle subscription management endpoints")
public class VehicleSubscriptionsController {
  private final VehicleSubscriptionCommandService vehicleSubscriptionCommandService;

  public VehicleSubscriptionsController(VehicleSubscriptionCommandService vehicleSubscriptionCommandService) {
    this.vehicleSubscriptionCommandService = vehicleSubscriptionCommandService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create a vehicle subscription")
  public ResponseEntity<VehicleSubscriptionResource> createVehicleSubscription(
      @RequestBody CreateVehicleSubscriptionResource resource) {
    var command = CreateVehicleSubscriptionCommandFromResourceAssembler.toCommandFromResource(resource);
    var vehicleSubscription = vehicleSubscriptionCommandService.handle(command).orElseThrow();
    var vehicleSubscriptionResource =
        VehicleSubscriptionResourceFromEntityAssembler.toResourceFromEntity(vehicleSubscription);
    return new ResponseEntity<>(vehicleSubscriptionResource, HttpStatus.CREATED);
  }
}
