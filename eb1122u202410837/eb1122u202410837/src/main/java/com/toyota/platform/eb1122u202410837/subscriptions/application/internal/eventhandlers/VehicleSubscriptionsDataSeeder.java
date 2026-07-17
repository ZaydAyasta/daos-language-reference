package com.toyota.platform.eb1122u202410837.subscriptions.application.internal.eventhandlers;

import com.toyota.platform.eb1122u202410837.shared.domain.valueobjects.Period;
import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.aggregates.VehicleSubscription;
import com.toyota.platform.eb1122u202410837.subscriptions.infrastructure.persistence.jpa.repositories.VehicleSubscriptionRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class VehicleSubscriptionsDataSeeder {
  private final VehicleSubscriptionRepository vehicleSubscriptionRepository;

  public VehicleSubscriptionsDataSeeder(VehicleSubscriptionRepository vehicleSubscriptionRepository) {
    this.vehicleSubscriptionRepository = vehicleSubscriptionRepository;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void seedVehicleSubscriptions() {
    var subscriptions = List.of(
        new VehicleSubscription("TCS-10001", "JTDBR32E720123456",
            new Period(LocalDate.parse("2026-01-01"), LocalDate.parse("2027-12-31")), null),
        new VehicleSubscription("TCS-10002", "2T1BURHE5JC654321",
            new Period(LocalDate.parse("2026-01-01"), LocalDate.parse("2028-06-15")), null),
        new VehicleSubscription("TCS-10003", "JTDKN3DU8A0123456",
            new Period(LocalDate.parse("2024-01-01"), LocalDate.parse("2025-01-01")), null),
        new VehicleSubscription("TCS-10004", "5YFBURHE6JP987654",
            new Period(LocalDate.parse("2026-01-01"), LocalDate.parse("2029-01-20")), null)
    );

    subscriptions.stream()
        .filter(subscription -> !vehicleSubscriptionRepository
            .existsBySubscriptionCode(subscription.getSubscriptionCode()))
        .forEach(vehicleSubscriptionRepository::save);
  }
}
