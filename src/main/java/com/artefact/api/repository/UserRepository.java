package com.artefact.api.repository;

import com.artefact.api.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    String UserBaseQuery = "Select u, r from User u join Role r on r.id = u.roleId ";
    String ByEmailQuery = UserBaseQuery + "where u.email = :email";
    String ByRoleQuery = UserBaseQuery + "where u.roleId = :id";

    @Query(ByEmailQuery)
    Optional<User> findByEmail(@Param("email") String email);

    @Query(ByRoleQuery)
    Iterable<User> findByRole(@Param("id") Long id);
}
