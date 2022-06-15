package com.artefact.api.repository;

import com.artefact.api.model.Order;
import com.artefact.api.model.OrderStatus;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface OrderStatusRepository extends PagingAndSortingRepository<OrderStatus, Long> {

}
