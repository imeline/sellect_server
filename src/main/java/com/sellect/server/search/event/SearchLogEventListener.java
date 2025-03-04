package com.sellect.server.search.event;

import com.sellect.server.search.domain.SearchLog;
import com.sellect.server.search.repository.SearchLogEntity;
import com.sellect.server.search.repository.SearchLogRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLogEventListener {

    private final SearchLogRepository searchLogRepository;

    @Async("asyncTaskExecutor") // 명시적으로 사용할 Executor 지정
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 검색 완료와는 별개의 트랜잭션 생성
    public void handleSearchLogEvent(SearchLogEvent event) {

        // 재시도는 하지 않기로 결정! (기획)
        try {
            log.info("[SearchLogEvent] 검색 로그 저장 시작 (스레드: {}): {}",
                Thread.currentThread().getName(), event.getSearchKeyword());

            // 이벤트 객체 → 도메인 객체 변환
            SearchLog searchLog = SearchLog.builder()
                .keyword(event.getSearchKeyword())
                .categoryId(event.getCategoryId())
                .brandId(event.getBrandId())
                .userIdentifier(event.getUserIdentifier())
                .timestamp(LocalDateTime.now())
                .resultCount(event.getResultCount())
                .filterApplied(event.isFilterApplied())
                .build();

            // 도메인 객체 → JPA 엔티티 변환 후 저장
            searchLogRepository.save(SearchLogEntity.from(searchLog).toModel());

            log.info("[SearchLogEvent] 검색 로그 저장 완료: {}", event.getSearchKeyword());
        } catch (Exception e) {
            // 검색 로그 데이터에 대한 재시도 로직은 하지 않기로 정함
            log.error("[SearchLogEvent] 검색 로그 저장 실패: {}", event.getSearchKeyword(), e);
        }
    }
}
