package org.sfa.request.repository;

import org.sfa.request.model.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Integer> {
    Optional<NotificationType> findByTypeName(String typeName);
}
