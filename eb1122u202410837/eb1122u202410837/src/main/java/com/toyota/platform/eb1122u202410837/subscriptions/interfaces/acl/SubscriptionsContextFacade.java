package com.toyota.platform.eb1122u202410837.subscriptions.interfaces.acl;

import com.toyota.platform.eb1122u202410837.shared.domain.exceptions.BusinessRuleException;
import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.toyota.platform.eb1122u202410837.subscriptions.infrastructure.persistence.jpa.repositories.VehicleSubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionsContextFacade {
  private final VehicleSubscriptionRepository vehicleSubscriptionRepository;

  public SubscriptionsContextFacade(VehicleSubscriptionRepository vehicleSubscriptionRepository) {
    this.vehicleSubscriptionRepository = vehicleSubscriptionRepository;
  }

  public SubscriptionSnapshot fetchSubscriptionById(Long subscriptionId) {
    var subscription = vehicleSubscriptionRepository.findById(subscriptionId)
        .orElseThrow(() -> new BusinessRuleException("vehicle-subscription.not-found"));
    return new SubscriptionSnapshot(
        subscription.getId(),
        subscription.getSubscriptionStatus(),
        subscription.isEligibleForAssistance()
    );
  }

  public record SubscriptionSnapshot(
      Long id,
      SubscriptionStatus subscriptionStatus,
      boolean eligibleForAssistance
  ) {
  }
}
