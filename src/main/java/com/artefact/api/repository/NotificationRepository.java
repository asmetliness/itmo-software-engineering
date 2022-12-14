package com.artefact.api.repository;

import com.artefact.api.model.Notification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface NotificationRepository extends PagingAndSortingRepository<Notification, Long> {

    @Query("Select n from Notification n where n.userId = :userId")
    Iterable<Notification> findByUserId(@Param("userId") long userId);
}
