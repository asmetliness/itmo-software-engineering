package com.artefact.api.repository;


import com.artefact.api.model.Weapon;
import com.artefact.api.repository.results.IWeaponResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface WeaponRepository extends PagingAndSortingRepository<Weapon, Long> {

    String BaseQuery = "Select " +
            "weapon, " +
            "status, " +
            "createdUser, " +
            "requestedUser, " +
            "acquiredUser, " +
            "suggestedCourier, " +
            "acceptedCourier " +
            "from Weapon weapon " +
            "join Status status on weapon.statusId = status.id " +
            "join User createdUser on weapon.createdUserId = createdUser.id " +
            "left join User requestedUser on weapon.requestedUserId = requestedUser.id " +
            "left join User acquiredUser on weapon.acquiredUserId = acquiredUser.id " +
            "left join User suggestedCourier on weapon.suggestedCourierId = suggestedCourier.id " +
            "left join User acceptedCourier on weapon.acceptedCourierId = acceptedCourier.id ";

    String ByCreatedUserQuery = BaseQuery + " where weapon.createdUserId = :userId";
    String ByAcquiredUserQuery = BaseQuery + " where weapon.acquiredUserId = :userId";
    String ByStatusQuery = BaseQuery + " where weapon.statusId = :statusId";
    String RequestedWeaponsQuery = BaseQuery + " where weapon.createdUserId = :userId AND weapon.requestedUserId is not null";
    String ByRequestedUserQuery = BaseQuery + " where weapon.requestedUserId = :userId";

    @Query(ByCreatedUserQuery)
    Iterable<IWeaponResult> findByCreatedUserId(@Param("userId") long userId);

    @Query(ByAcquiredUserQuery)
    Iterable<IWeaponResult> findByAcquiredUserId(@Param("userId") long userId);

    @Query(ByStatusQuery)
    Iterable<IWeaponResult> findByStatusId(@Param("statusId") long statusId);

    @Query(RequestedWeaponsQuery)
    Iterable<IWeaponResult> findRequestedWeapons(@Param("userId") long userId);

    @Query(ByRequestedUserQuery)
    Iterable<IWeaponResult> findByRequestedUserId(@Param("userId") long userId);
}
