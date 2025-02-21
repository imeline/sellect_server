package com.sellect.server.search.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AutoCompleteKeyword {

    private final Long id;
    private final String keyword;
    private Long frequency;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deleteAt;

    // frequency 증가 메서드
    public AutoCompleteKeyword incrementFrequency(long count) {
        return AutoCompleteKeyword.builder()
            .id(this.id)
            .keyword(this.keyword)
            .frequency(this.frequency += count)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .deleteAt(this.deleteAt)
            .build();
    }

}