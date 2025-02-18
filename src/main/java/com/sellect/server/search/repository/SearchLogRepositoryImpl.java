package com.sellect.server.search.repository;

import com.sellect.server.search.domain.SearchLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchLogRepositoryImpl implements SearchLogRepository{

    private final SearchLogJpaRepository searchLogJpaRepository;

    @Override
    public SearchLog save(SearchLog searchLog) {
        return searchLogJpaRepository.save(SearchLogEntity.from(searchLog)).toModel();
    }
}
