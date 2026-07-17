package com.toyota.platform.eb1122u202410837.subscriptions.interfaces.rest.transform;

import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.aggregates.VehicleSubscription;
import com.toyota.platform.eb1122u202410837.subscriptions.interfaces.rest.resources.VehicleSubscriptionResource;

public class VehicleSubscriptionResourceFromEntityAssembler {
  private VehicleSubscriptionResourceFromEntityAssembler() {
  }

  public static VehicleSubscriptionResource toResourceFromEntity(VehicleSubscription entity) {
    return new VehicleSubscriptionResource(
        entity.getId(),
        entity.getSubscriptionCode(),
        entity.getVehicleVin(),
        entity.getServicePeriod().startDate().toString(),
        entity.getServicePeriod().endDate().toString(),
        entity.getLastAssistanceRequestId(),
        entity.getSubscriptionStatus().name()
    );
  }
}
