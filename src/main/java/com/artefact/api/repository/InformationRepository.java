package com.artefact.api.repository;

import com.artefact.api.model.Information;
import com.artefact.api.repository.results.IInformationResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface InformationRepository extends PagingAndSortingRepository<Information, Long> {

    String BaseQuery = "Select " +
            "information as information, " +
            "createdUser as createdUser, " +
            "acquiredUser as acquiredUser, " +
            "requestedUser as requestedUser, " +
            "status as status " +
            "from Information information " +
            "join User createdUser on information.createdUserId = createdUser.id " +
            "join Status status on information.statusId = status.id " +
            "left join User acquiredUser on information.acquiredUserId = acquiredUser.id " +
            "left join User requestedUser on information.requestedUserId = requestedUser.id";

    String FindByStatusQuery = BaseQuery + " where information.statusId = :statusId";
    String FindByAcquiredQuery = BaseQuery + " where information.acquiredUserId = :userId";
    String FindByCreatedQuery = BaseQuery + " where information.createdUserId = :userId";
    String FindNotAcceptedQuery = BaseQuery + " where information.acquiredUserId is null";

    String FindRequestedQuery = BaseQuery + " where information.createdUserId = :userId AND information.requestedUserId is not null";

    String FindByRequestedUserQuery = BaseQuery + " where information.requestedUserId = :userId";

    String FindByInformationId = BaseQuery + " where information.id = :id";

    @Query(FindByStatusQuery)
    Iterable<IInformationResult> findByStatus(@Param("statusId") long statusId);

    @Query(FindByAcquiredQuery)
    Iterable<IInformationResult> findByAcquiredUser(@Param("userId") long userId);

    @Query(FindByCreatedQuery)
    Iterable<IInformationResult> findByCreatedUser(@Param("userId") long userId);

    @Query(FindNotAcceptedQuery)
    Iterable<IInformationResult> findAllNotAccepted();

    @Query(FindRequestedQuery)
    Iterable<IInformationResult> findRequestedInformation(@Param("userId")long userId);

    @Query(FindByRequestedUserQuery)
    Iterable<IInformationResult> findByRequestedUserId(@Param("userId")long userId);

    @Query(FindByInformationId)
    Optional<IInformationResult> findByInformationId(@Param("id") long id);
}
