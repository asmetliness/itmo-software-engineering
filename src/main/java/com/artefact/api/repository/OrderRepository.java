package com.artefact.api.repository;

import com.artefact.api.model.Order;
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
            "join Artifact a on s.artifactId = a.id" +
            "join User cu on s.createdUserId = cu.id" +
            "join User au on s.acceptedUserId = au.id" +
            "join User asu on s.assignedUserId = asu.id" +
            "join User su on s.suggestedUserId = su.id" +
            "where o.createdUserId = :userId")
    Iterable<Order> findByCreatedUserId(@Param("userId") long userId);

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
            "join Artifact a on s.artifactId = a.id" +
            "join User cu on s.createdUserId = cu.id" +
            "join User au on s.acceptedUserId = au.id" +
            "join User asu on s.assignedUserId = asu.id" +
            "join User su on s.suggestedUserId = su.id" +
            "where o.assignedUserId = :userId")
    Iterable<Order> findByAssignedUserId(@Param("userId") long userId);

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
            "join Artifact a on s.artifactId = a.id" +
            "join User cu on s.createdUserId = cu.id" +
            "join User au on s.acceptedUserId = au.id" +
            "join User asu on s.assignedUserId = asu.id" +
            "join User su on s.suggestedUserId = su.id" +
            "where o.acceptedUserId = :userId")
    Iterable<Order> findByAcceptedUserId(@Param("userId") long userId);

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
            "join Artifact a on s.artifactId = a.id" +
            "join User cu on s.createdUserId = cu.id" +
            "join User au on s.acceptedUserId = au.id" +
            "join User asu on s.assignedUserId = asu.id" +
            "join User su on s.suggestedUserId = su.id" +
            "where o.statusId = :statusId")
    Iterable<Order> findOrderByStatus(@Param("statusId") long statusId);

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
            "join Artifact a on s.artifactId = a.id" +
            "join User cu on s.createdUserId = cu.id" +
            "join User au on s.acceptedUserId = au.id" +
            "join User asu on s.assignedUserId = asu.id" +
            "join User su on s.suggestedUserId = su.id" +
            "where o.suggestedUserId = :userId")
    Iterable<Order> findSuggestedOrders(@Param("userId") long userId);
}

