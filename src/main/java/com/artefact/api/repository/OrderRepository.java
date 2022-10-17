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

    @Query("Select " +
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
            "left join User su on o.suggestedUserId = su.id " +
            "where o.createdUserId = :userId")
    Iterable<IOrderResult> findByCreatedUserId(@Param("userId") long userId);

    @Query("Select " +
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
            "left join User su on o.suggestedUserId = su.id " +
            "where o.assignedUserId = :userId")
    Iterable<IOrderResult> findByAssignedUserId(@Param("userId") long userId);

    @Query("Select " +
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
            "left join User su on o.suggestedUserId = su.id " +
            "where o.acceptedUserId = :userId")
    Iterable<IOrderResult> findByAcceptedUserId(@Param("userId") long userId);

    @Query("Select " +
            "o as order, " +
            "s as status, " +
            "a as artifact, " +
            "cu as createdUser, " +
            "au as acceptedUser, " +
            "asu as assignedUser, " +
            "su as suggestedUser " +
            "from Order o " +
            "join OrderStatus s on o.statusId = s.id " +
            "join Artifact a on o.artifactId = a.id " +
            "join User cu on o.createdUserId = cu.id " +
            "left join User au on o.acceptedUserId = au.id " +
            "left join User asu on o.assignedUserId = asu.id " +
            "left join User su on o.suggestedUserId = su.id " +
            "where o.statusId = :statusId")
    Iterable<IOrderResult> findOrderByStatus(@Param("statusId") long statusId);

    @Query("Select " +
            "o as order, " +
            "s as status," +
            "a as artifact, " +
            "cu as createdUser, " +
            "au as acceptedUser, " +
            "asu as assignedUser, " +
            "su as suggestedUser " +
            "from Order o " +
            "join OrderStatus s on o.statusId = s.id " +
            "join Artifact a on o.artifactId = a.id " +
            "join User cu on o.createdUserId = cu.id " +
            "left join User au on o.acceptedUserId = au.id " +
            "left join User asu on o.assignedUserId = asu.id " +
            "left join User su on o.suggestedUserId = su.id " +
            "where o.suggestedUserId = :userId")
    Iterable<IOrderResult> findSuggestedOrders(@Param("userId") long userId);
}

