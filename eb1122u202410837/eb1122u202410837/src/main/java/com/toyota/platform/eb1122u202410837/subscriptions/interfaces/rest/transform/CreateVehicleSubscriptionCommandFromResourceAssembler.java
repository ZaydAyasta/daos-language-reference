package com.toyota.platform.eb1122u202410837.subscriptions.interfaces.rest.transform;

import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.commands.CreateVehicleSubscriptionCommand;
import com.toyota.platform.eb1122u202410837.subscriptions.interfaces.rest.resources.CreateVehicleSubscriptionResource;
import java.time.LocalDate;

public class CreateVehicleSubscriptionCommandFromResourceAssembler {
  private CreateVehicleSubscriptionCommandFromResourceAssembler() {
  }

  public static CreateVehicleSubscriptionCommand toCommandFromResource(
      CreateVehicleSubscriptionResource resource) {
    return new CreateVehicleSubscriptionCommand(
        resource.subscriptionCode(),
        resource.vehicleVin(),
        LocalDate.parse(resource.startDate()),
        LocalDate.parse(resource.endDate())
    );
  }
}
