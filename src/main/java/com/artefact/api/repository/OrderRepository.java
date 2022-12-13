package com.artefact.api.repository;

import com.artefact.api.model.Order;
import com.artefact.api.repository.results.IOrderResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {
    String BaseOrderQuery = "Select " +
            "order, " +
            "status," +
            "artifact," +
            "createdUser," +
            "acceptedUser," +
            "assignedUser," +
            "suggestedUser, " +
            "acceptedCourier " +
            "from Order order " +
            "join OrderStatus status on order.statusId = status.id " +
            "join Artifact artifact on order.artifactId = artifact.id " +
            "join User createdUser on order.createdUserId = createdUser.id " +
            "left join User acceptedUser on order.acceptedUserId = acceptedUser.id " +
            "left join User assignedUser on order.assignedUserId = assignedUser.id " +
            "left join User suggestedUser on order.suggestedUserId = suggestedUser.id " +
            "left join User acceptedCourier on order.acceptedCourierId = acceptedCourier.id";

    String ByCreatedUserQuery     = BaseOrderQuery + " where order.createdUserId = :userId";
    String ByAssignedUserQuery    = BaseOrderQuery + " where order.assignedUserId = :userId";
    String ByAcceptedUserQuery    = BaseOrderQuery + " where order.acceptedUserId = :userId";
    String ByOrderStatusQuery     = BaseOrderQuery + " where order.statusId = :statusId";
    String BySuggestedUserQuery   = BaseOrderQuery + " where order.suggestedUserId = :userId";


    @Query(ByCreatedUserQuery)
    Iterable<IOrderResult> findByCreatedUserId(@Param("userId") long userId);

    @Query(ByAssignedUserQuery)
    Iterable<IOrderResult> findByAssignedUserId(@Param("userId") long userId);

    @Query(ByAcceptedUserQuery)
    Iterable<IOrderResult> findByAcceptedUserId(@Param("userId") long userId);

    @Query(ByOrderStatusQuery)
    Iterable<IOrderResult> findOrderByStatus(@Param("statusId") long statusId);

    @Query(BySuggestedUserQuery)
    Iterable<IOrderResult> findSuggestedOrders(@Param("userId") long userId);

}

