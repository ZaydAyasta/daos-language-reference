package com.toyota.platform.eb1122u202410837.subscriptions.domain.model.aggregates;

import com.toyota.platform.eb1122u202410837.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.toyota.platform.eb1122u202410837.shared.domain.valueobjects.Period;
import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.commands.CreateVehicleSubscriptionCommand;
import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class VehicleSubscription extends AuditableAbstractAggregateRoot<VehicleSubscription> {

  @Column(nullable = false, unique = true)
  private String subscriptionCode;

  @Column(nullable = false, unique = true, length = 17)
  private String vehicleVin;

  @Embedded
  private Period servicePeriod;

  private Long lastAssistanceRequestId;

  @Version
  private Long version;

  public VehicleSubscription(String subscriptionCode, String vehicleVin, Period servicePeriod,
                             Long lastAssistanceRequestId) {
    this.subscriptionCode = validateSubscriptionCode(subscriptionCode);
    this.vehicleVin = validateVehicleVin(vehicleVin);
    this.servicePeriod = validateServicePeriod(servicePeriod);
    this.lastAssistanceRequestId = lastAssistanceRequestId;
  }

  public VehicleSubscription(CreateVehicleSubscriptionCommand command) {
    this(
        command.subscriptionCode(),
        command.vehicleVin(),
        new Period(command.startDate(), command.endDate()),
        null
    );
  }

  public SubscriptionStatus getSubscriptionStatus() {
    return servicePeriod.includes(LocalDate.now())
        ? SubscriptionStatus.ACTIVE
        : SubscriptionStatus.EXPIRED;
  }

  public boolean isEligibleForAssistance() {
    return lastAssistanceRequestId == null;
  }

  public void markAssistanceRequestInProgress(Long assistanceRequestId) {
    if (assistanceRequestId == null) {
      throw new IllegalArgumentException("Assistance request id cannot be null");
    }
    if (lastAssistanceRequestId != null && lastAssistanceRequestId.equals(assistanceRequestId)) {
      return;
    }
    if (lastAssistanceRequestId != null) {
      throw new IllegalStateException("Subscription already has an assistance request in progress");
    }
    lastAssistanceRequestId = assistanceRequestId;
  }

  private String validateSubscriptionCode(String subscriptionCode) {
    if (subscriptionCode == null || !subscriptionCode.matches("TCS-\\d{5}")) {
      throw new IllegalArgumentException("Subscription code must match format TCS-XXXXX");
    }
    return subscriptionCode;
  }

  private String validateVehicleVin(String vehicleVin) {
    if (vehicleVin == null || vehicleVin.length() != 17) {
      throw new IllegalArgumentException("Vehicle VIN must contain exactly 17 characters");
    }
    return vehicleVin;
  }

  private Period validateServicePeriod(Period servicePeriod) {
    if (servicePeriod == null || servicePeriod.startDate() == null || servicePeriod.endDate() == null) {
      throw new IllegalArgumentException("Service period dates cannot be null");
    }
    return servicePeriod;
  }
}
