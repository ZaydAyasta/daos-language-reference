package com.toyota.platform.eb1122u202410837.assistance.infrastructure.persistence.jpa.repositories;

import com.toyota.platform.eb1122u202410837.assistance.domain.model.aggregates.AssistanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssistanceRequestRepository extends JpaRepository<AssistanceRequest, Long> {
}
