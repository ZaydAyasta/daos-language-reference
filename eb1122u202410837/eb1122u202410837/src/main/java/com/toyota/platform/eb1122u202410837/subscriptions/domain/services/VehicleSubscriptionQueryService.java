package com.toyota.platform.eb1122u202410837.subscriptions.domain.services;

import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.aggregates.VehicleSubscription;
import java.util.Optional;

public interface VehicleSubscriptionQueryService {
  Optional<VehicleSubscription> findById(Long id);
}
