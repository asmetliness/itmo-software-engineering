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
            "o, " +
            "s," +
            "a," +
            "cu," +
            "au," +
            "asu," +
            "su " +
            "from Order o " +
            "join OrderStatus s on o.statusId = s.id " +
            "join Artifact a on o.artifactId = a.id " +
            "join User cu on o.createdUserId = cu.id " +
            "left join User au on o.acceptedUserId = au.id " +
            "left join User asu on o.assignedUserId = asu.id " +
            "left join User su on o.suggestedUserId = su.id ";

    String ByCreatedUserQuery     = BaseOrderQuery + " where o.createdUserId = :userId";
    String ByAssignedUserQuery    = BaseOrderQuery + " where o.assignedUserId = :userId";
    String ByAcceptedUserQuery    = BaseOrderQuery + " where o.acceptedUserId = :userId";
    String ByOrderStatusQuery     = BaseOrderQuery + " where o.statusId = :statusId";
    String BySuggestedUserQuery   = BaseOrderQuery + " where o.suggestedUserId = :userId";


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

