package com.artefact.api.repository;

import com.artefact.api.model.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {
    @Query("select r from Role r where r.name = :name")
    Role findByName(@Param("name") String name);
}
