package com.artefact.api.repository;

import com.artefact.api.model.Artifact;
import com.artefact.api.model.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ArtifactRepository extends PagingAndSortingRepository<Artifact, Long> {

}
