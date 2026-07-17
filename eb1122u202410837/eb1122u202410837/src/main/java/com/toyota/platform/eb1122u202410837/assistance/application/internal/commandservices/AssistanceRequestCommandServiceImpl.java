package com.toyota.platform.eb1122u202410837.assistance.application.internal.commandservices;

import com.toyota.platform.eb1122u202410837.assistance.domain.model.aggregates.AssistanceRequest;
import com.toyota.platform.eb1122u202410837.assistance.domain.model.commands.CreateAssistanceRequestCommand;
import com.toyota.platform.eb1122u202410837.assistance.domain.model.events.AssistanceRequestCreatedEvent;
import com.toyota.platform.eb1122u202410837.assistance.domain.services.AssistanceRequestCommandService;
import com.toyota.platform.eb1122u202410837.assistance.infrastructure.persistence.jpa.repositories.AssistanceRequestRepository;
import com.toyota.platform.eb1122u202410837.shared.domain.exceptions.BusinessRuleException;
import com.toyota.platform.eb1122u202410837.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.toyota.platform.eb1122u202410837.subscriptions.interfaces.acl.SubscriptionsContextFacade;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssistanceRequestCommandServiceImpl implements AssistanceRequestCommandService {
  private final AssistanceRequestRepository assistanceRequestRepository;
  private final SubscriptionsContextFacade subscriptionsContextFacade;
  private final ApplicationEventPublisher applicationEventPublisher;

  public AssistanceRequestCommandServiceImpl(AssistanceRequestRepository assistanceRequestRepository,
                                             SubscriptionsContextFacade subscriptionsContextFacade,
                                             ApplicationEventPublisher applicationEventPublisher) {
    this.assistanceRequestRepository = assistanceRequestRepository;
    this.subscriptionsContextFacade = subscriptionsContextFacade;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  @Transactional
  public Optional<AssistanceRequest> handle(CreateAssistanceRequestCommand command) {
    var subscription = subscriptionsContextFacade.fetchSubscriptionById(command.subscriptionId());
    if (subscription.subscriptionStatus() == SubscriptionStatus.EXPIRED) {
      throw new BusinessRuleException("assistance-request.subscription-expired");
    }
    if (!subscription.eligibleForAssistance()) {
      throw new BusinessRuleException("assistance-request.subscription-not-eligible");
    }
    var assistanceRequest = assistanceRequestRepository.save(new AssistanceRequest(command));
    applicationEventPublisher.publishEvent(new AssistanceRequestCreatedEvent(
        assistanceRequest.getSubscriptionId(),
        assistanceRequest.getId(),
        assistanceRequest.getRequestedAt()
    ));
    return Optional.of(assistanceRequest);
  }
}
