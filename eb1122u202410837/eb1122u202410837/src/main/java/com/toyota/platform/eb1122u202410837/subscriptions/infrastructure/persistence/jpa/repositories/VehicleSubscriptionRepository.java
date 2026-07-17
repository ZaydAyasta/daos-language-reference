package com.toyota.platform.eb1122u202410837.subscriptions.infrastructure.persistence.jpa.repositories;

import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.aggregates.VehicleSubscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleSubscriptionRepository extends JpaRepository<VehicleSubscription, Long> {
  boolean existsBySubscriptionCode(String subscriptionCode);

  boolean existsByVehicleVin(String vehicleVin);

  Optional<VehicleSubscription> findBySubscriptionCode(String subscriptionCode);
}
