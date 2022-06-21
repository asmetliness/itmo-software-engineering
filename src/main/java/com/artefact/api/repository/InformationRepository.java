package com.artefact.api.repository;

import com.artefact.api.model.Information;
import com.artefact.api.model.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface InformationRepository extends PagingAndSortingRepository<Information, Long> {
    @Query("Select i from Information i where i.statusId = :statusId")
    Iterable<Information> findByStatus(@Param("statusId") long statusId);

    @Query("Select i from Information i where i.acceptedUserId = :userId")
    Iterable<Information> findByAcceptedUser(@Param("userId") long userId);

    @Query("Select i from Information i where i.createdUserId = :userId")
    Iterable<Information> findByCreatedUser(@Param("userId") long userId);
}
