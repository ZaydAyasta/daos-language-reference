package com.toyota.platform.eb1122u202410837.shared.domain.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public record Period(
    @Column(nullable = false) LocalDate startDate,
    @Column(nullable = false) LocalDate endDate
) {
  public Period() {
    this(null, null);
  }

  public Period {
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }
  }

  public boolean includes(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }
}
