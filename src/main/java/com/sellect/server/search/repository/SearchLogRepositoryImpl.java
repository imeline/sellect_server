package com.sellect.server.search.repository;

import com.sellect.server.search.domain.SearchLog;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchLogRepositoryImpl implements SearchLogRepository {

    private final SearchLogJpaRepository searchLogJpaRepository;
    private final EntityManager entityManager;


    @Override
    public SearchLog save(SearchLog searchLog) {
        return searchLogJpaRepository.save(SearchLogEntity.from(searchLog)).toModel();
    }

    // todo: check 사실 엔티티 객체를 조회하는 것이 아니기에 영속성 컨텍스트를 사용하지 않음) -> JdbcTemplate 동일한 방식으로 동작
    @Override
    public List<Object[]> findSearchLogsForBatch(LocalDateTime startTime, LocalDateTime endTime) {
        return entityManager.createQuery(
                "SELECT s.keyword, s.userIdentifier FROM SearchLogEntity s " +
                    "WHERE s.timestamp BETWEEN :startTime AND :endTime " +
                    "AND s.filterApplied = false", Object[].class)
            .setParameter("startTime", startTime)
            .setParameter("endTime", endTime)
            .getResultList();
    }

}
