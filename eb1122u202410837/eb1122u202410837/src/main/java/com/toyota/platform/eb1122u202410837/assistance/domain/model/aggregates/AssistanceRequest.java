package com.toyota.platform.eb1122u202410837.assistance.domain.model.aggregates;

import com.toyota.platform.eb1122u202410837.assistance.domain.model.commands.CreateAssistanceRequestCommand;
import com.toyota.platform.eb1122u202410837.assistance.domain.model.valueobjects.AssistanceStatus;
import com.toyota.platform.eb1122u202410837.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class AssistanceRequest extends AuditableAbstractAggregateRoot<AssistanceRequest> {

  @Column(nullable = false)
  private Long subscriptionId;

  @Column(nullable = false)
  private String issueDescription;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AssistanceStatus assistanceStatus;

  @Column(nullable = false)
  private LocalDateTime requestedAt;

  public AssistanceRequest(Long subscriptionId, String issueDescription,
                           AssistanceStatus assistanceStatus, LocalDateTime requestedAt) {
    this.subscriptionId = validateSubscriptionId(subscriptionId);
    this.issueDescription = validateIssueDescription(issueDescription);
    this.assistanceStatus = validateAssistanceStatus(assistanceStatus);
    this.requestedAt = validateRequestedAt(requestedAt);
  }

  public AssistanceRequest(CreateAssistanceRequestCommand command) {
    this(
        command.subscriptionId(),
        command.issueDescription(),
        command.assistanceStatus(),
        command.requestedAt()
    );
  }

  private Long validateSubscriptionId(Long subscriptionId) {
    if (subscriptionId == null || subscriptionId <= 0) {
      throw new IllegalArgumentException("Subscription id must be greater than zero");
    }
    return subscriptionId;
  }

  private String validateIssueDescription(String issueDescription) {
    if (issueDescription == null || issueDescription.isBlank()) {
      throw new IllegalArgumentException("Issue description cannot be null or blank");
    }
    return issueDescription;
  }

  private AssistanceStatus validateAssistanceStatus(AssistanceStatus assistanceStatus) {
    if (assistanceStatus == null) {
      throw new IllegalArgumentException("Assistance status cannot be null");
    }
    return assistanceStatus;
  }

  private LocalDateTime validateRequestedAt(LocalDateTime requestedAt) {
    if (requestedAt == null) {
      throw new IllegalArgumentException("Requested at cannot be null");
    }
    if (requestedAt.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("Requested at cannot be in the future");
    }
    return requestedAt;
  }
}
