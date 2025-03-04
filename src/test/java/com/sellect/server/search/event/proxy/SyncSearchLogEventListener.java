package com.sellect.server.search.event.proxy;

import com.sellect.server.search.event.SearchLogEvent;
import com.sellect.server.search.event.SearchLogEventListener;
import com.sellect.server.search.repository.SearchLogRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SyncSearchLogEventListener extends SearchLogEventListener {
    public SyncSearchLogEventListener(SearchLogRepository searchLogRepository) {
        super(searchLogRepository);
    }

    @Override
    public void handleSearchLogEvent(SearchLogEvent event) {
        super.handleSearchLogEvent(event); // ✅ 동기 실행
    }
}
