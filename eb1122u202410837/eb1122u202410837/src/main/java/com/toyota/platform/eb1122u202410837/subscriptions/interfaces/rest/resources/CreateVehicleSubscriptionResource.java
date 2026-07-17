package com.toyota.platform.eb1122u202410837.subscriptions.interfaces.rest.resources;

public record CreateVehicleSubscriptionResource(
    String subscriptionCode,
    String vehicleVin,
    String startDate,
    String endDate
) {
}
