package com.toyota.platform.eb1122u202410837.subscriptions.application.internal.eventhandlers;

import com.toyota.platform.eb1122u202410837.assistance.domain.model.events.AssistanceRequestCreatedEvent;
import com.toyota.platform.eb1122u202410837.subscriptions.infrastructure.persistence.jpa.repositories.VehicleSubscriptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AssistanceRequestCreatedEventHandler {
  private final VehicleSubscriptionRepository vehicleSubscriptionRepository;

  public AssistanceRequestCreatedEventHandler(VehicleSubscriptionRepository vehicleSubscriptionRepository) {
    this.vehicleSubscriptionRepository = vehicleSubscriptionRepository;
  }

  @EventListener
  @Transactional
  public void on(AssistanceRequestCreatedEvent event) {
    var subscription = vehicleSubscriptionRepository.findById(event.subscriptionId())
        .orElseThrow(() -> new IllegalArgumentException("Vehicle subscription not found"));
    subscription.markAssistanceRequestInProgress(event.assistanceRequestId());
    vehicleSubscriptionRepository.save(subscription);
  }
}
