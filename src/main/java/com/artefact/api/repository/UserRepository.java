package com.artefact.api.repository;

import com.artefact.api.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    @Query("Select u from User u where u.login = :login")
    User getByLogin(@Param("login") String login);
}
