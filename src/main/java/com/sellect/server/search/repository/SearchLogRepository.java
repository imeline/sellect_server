package com.sellect.server.search.repository;

import com.sellect.server.search.domain.SearchLog;
import java.time.LocalDateTime;
import java.util.List;

public interface SearchLogRepository {

    SearchLog save(SearchLog searchLog);

    List<Object[]> findSearchLogsForBatch(LocalDateTime startTime, LocalDateTime endTime);

}
