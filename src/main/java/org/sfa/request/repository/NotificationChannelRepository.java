package org.sfa.request.repository;

import org.sfa.request.model.entity.NotificationChannelRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannelRef, Integer> {
    Optional<NotificationChannelRef> findByChannelName(String channelName);
}
