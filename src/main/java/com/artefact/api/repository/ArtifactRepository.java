package com.artefact.api.repository;

import com.artefact.api.model.Artifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ArtifactRepository extends JpaRepository<Artifact, Long> {

}
