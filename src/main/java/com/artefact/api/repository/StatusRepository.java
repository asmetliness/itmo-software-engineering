package com.artefact.api.repository;

import com.artefact.api.model.Order;
import com.artefact.api.model.Status;
import com.artefact.api.repository.results.IOrderResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface StatusRepository extends JpaRepository<Status, Long> {

}

