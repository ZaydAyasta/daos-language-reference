package com.toyota.platform.eb1122u202410837.subscriptions.application.internal.queryservices;

import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.aggregates.VehicleSubscription;
import com.toyota.platform.eb1122u202410837.subscriptions.domain.services.VehicleSubscriptionQueryService;
import com.toyota.platform.eb1122u202410837.subscriptions.infrastructure.persistence.jpa.repositories.VehicleSubscriptionRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class VehicleSubscriptionQueryServiceImpl implements VehicleSubscriptionQueryService {
  private final VehicleSubscriptionRepository vehicleSubscriptionRepository;

  public VehicleSubscriptionQueryServiceImpl(VehicleSubscriptionRepository vehicleSubscriptionRepository) {
    this.vehicleSubscriptionRepository = vehicleSubscriptionRepository;
  }

  @Override
  public Optional<VehicleSubscription> findById(Long id) {
    return vehicleSubscriptionRepository.findById(id);
  }
}
