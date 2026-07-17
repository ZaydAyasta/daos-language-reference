package com.toyota.platform.eb1122u202410837.subscriptions.domain.services;

import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.aggregates.VehicleSubscription;
import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.commands.CreateVehicleSubscriptionCommand;
import java.util.Optional;

public interface VehicleSubscriptionCommandService {
  Optional<VehicleSubscription> handle(CreateVehicleSubscriptionCommand command);
}
