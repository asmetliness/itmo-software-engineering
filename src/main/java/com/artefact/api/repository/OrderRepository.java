package com.artefact.api.repository;

import com.artefact.api.model.Artifact;
import com.artefact.api.model.Order;
import com.artefact.api.model.OrderStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {
    @Query("Select o from Order o where o.createdUserId = :userId")
    Iterable<Order> findByCreatedUserId(@Param("userId") long userId);

    @Query("Select o from Order o where o.assignedUserId = :userId")
    Iterable<Order> findByAssignedUserId(@Param("userId") long userId);

    @Query("Select o from Order o where o.acceptedUserId = :userId")
    Iterable<Order> findByAcceptedUserId(@Param("userId") long userId);

    @Query("Select o from Order o where o.statusId = :statusId")
    Iterable<Order> findOrderByStatus(@Param("statusId") long statusId);

    @Query("Select o from Order o where o.suggestedUserId = :userId")
    Iterable<Order> findSuggestedOrders(@Param("userId") long userId);
}
