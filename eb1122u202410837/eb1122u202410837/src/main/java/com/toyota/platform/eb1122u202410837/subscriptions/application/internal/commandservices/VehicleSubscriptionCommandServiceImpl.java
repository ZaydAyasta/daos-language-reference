package com.toyota.platform.eb1122u202410837.subscriptions.application.internal.commandservices;

import com.toyota.platform.eb1122u202410837.shared.domain.exceptions.BusinessRuleException;
import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.aggregates.VehicleSubscription;
import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.commands.CreateVehicleSubscriptionCommand;
import com.toyota.platform.eb1122u202410837.subscriptions.domain.services.VehicleSubscriptionCommandService;
import com.toyota.platform.eb1122u202410837.subscriptions.infrastructure.persistence.jpa.repositories.VehicleSubscriptionRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class VehicleSubscriptionCommandServiceImpl implements VehicleSubscriptionCommandService {
  private final VehicleSubscriptionRepository vehicleSubscriptionRepository;

  public VehicleSubscriptionCommandServiceImpl(VehicleSubscriptionRepository vehicleSubscriptionRepository) {
    this.vehicleSubscriptionRepository = vehicleSubscriptionRepository;
  }

  @Override
  public Optional<VehicleSubscription> handle(CreateVehicleSubscriptionCommand command) {
    if (vehicleSubscriptionRepository.existsBySubscriptionCode(command.subscriptionCode())) {
      throw new BusinessRuleException("vehicle-subscription.subscription-code.exists");
    }
    if (vehicleSubscriptionRepository.existsByVehicleVin(command.vehicleVin())) {
      throw new BusinessRuleException("vehicle-subscription.vehicle-vin.exists");
    }
    var vehicleSubscription = new VehicleSubscription(command);
    return Optional.of(vehicleSubscriptionRepository.save(vehicleSubscription));
  }
}
