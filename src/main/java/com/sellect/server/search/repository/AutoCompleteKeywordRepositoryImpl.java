package com.sellect.server.search.repository;

import com.sellect.server.search.domain.AutoCompleteKeyword;
import com.sellect.server.search.repository.jpa.AutoCompleteKeywordEntity;
import com.sellect.server.search.repository.jpa.AutoCompleteKeywordJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AutoCompleteKeywordRepositoryImpl implements AutoCompleteKeywordRepository {

    private static final int MAX_LIMIT = 10;

    private final AutoCompleteKeywordJpaRepository autoCompleteKeywordJpaRepository;

    @Override
    public List<AutoCompleteKeyword> findTopKeywordsStartingWith(String query) {
        Pageable pageable = PageRequest.of(0, MAX_LIMIT, Direction.DESC, "frequency");
        List<AutoCompleteKeywordEntity> findKeywords = autoCompleteKeywordJpaRepository
            .findTopByKeywordStartingWith(query, pageable);
        return findKeywords.stream()
            .map(AutoCompleteKeywordEntity::toModel)
            .toList();
    }

    // todo: 기존 데이터 조회 + 업데이트 (JPA 사용) -> AutoCompleteKeyword Entity 조회 영속성 컨텍스트를 거친다.
    @Override
    public Optional<AutoCompleteKeyword> findByKeyword(String keyword) {
        return autoCompleteKeywordJpaRepository.findByKeywordAndDeleteAtIsNull(keyword)
            .map(AutoCompleteKeywordEntity::toModel);
    }

    @Override
    public AutoCompleteKeyword save(AutoCompleteKeyword autoCompleteKeyword) {
        return autoCompleteKeywordJpaRepository.save(
            AutoCompleteKeywordEntity.from(autoCompleteKeyword)).toModel();
    }
}
