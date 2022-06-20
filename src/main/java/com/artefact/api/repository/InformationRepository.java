package com.artefact.api.repository;

import com.artefact.api.model.Information;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface InformationRepository extends PagingAndSortingRepository<Information, Long> {

    // TODO: Поменять на джоин таблиц. Пока захардкоржено
    @Query("Select i from Information i where i.statusId = 1") // 1 = OrderStatusIds.NewOrder
    Iterable<Information> getAll();
}
