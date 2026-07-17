package com.toyota.platform.eb1122u202410837.subscriptions.domain.model.commands;

import java.time.LocalDate;

public record CreateVehicleSubscriptionCommand(
    String subscriptionCode,
    String vehicleVin,
    LocalDate startDate,
    LocalDate endDate
) {
}
