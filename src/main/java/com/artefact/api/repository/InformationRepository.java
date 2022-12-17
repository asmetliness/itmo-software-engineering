package com.artefact.api.repository;

import com.artefact.api.model.Information;
import com.artefact.api.repository.results.IInformationResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface InformationRepository extends PagingAndSortingRepository<Information, Long> {

    String BaseQuery = "Select " +
            "information as information, " +
            "createdUser as createdUser, " +
            "acquiredUser as acquiredUser " +
            "from Information information " +
            "join User createdUser on information.createdUserId = createdUser.id " +
            "left join User acquiredUser on information.acquiredUserId = acquiredUser.id";

    String FindByStatusQuery = BaseQuery + " where information.statusId = :statusId";
    String FindByAcceptedQuery = BaseQuery + " where information.acquiredUserId = :userId";
    String FindByCreatedQuery = BaseQuery + " where information.createdUserId = :userId";
    String FindNotAcceptedQuery = BaseQuery + " where information.acquiredUserId is null";


    @Query(FindByStatusQuery)
    Iterable<IInformationResult> findByStatus(@Param("statusId") long statusId);

    @Query(FindByAcceptedQuery)
    Iterable<IInformationResult> findByAcceptedUser(@Param("userId") long userId);

    @Query(FindByCreatedQuery)
    Iterable<IInformationResult> findByCreatedUser(@Param("userId") long userId);

    @Query(FindNotAcceptedQuery)
    Iterable<IInformationResult> findAllNotAccepted();
}
