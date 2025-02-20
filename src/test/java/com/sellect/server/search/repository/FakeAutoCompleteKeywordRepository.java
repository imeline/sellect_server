package com.sellect.server.search.repository;

import com.sellect.server.search.domain.AutoCompleteKeyword;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FakeAutoCompleteKeywordRepository implements AutoCompleteKeywordRepository {

    private static final int MAX_LIMIT = 10;

    private final List<AutoCompleteKeyword> data = new ArrayList<>();

    @Override
    public List<AutoCompleteKeyword> findTopKeywordsStartingWith(String query) {
        return data.stream()
            .filter(searchKeyword -> searchKeyword.getKeyword().startsWith(query))
            .sorted(Comparator.comparing(AutoCompleteKeyword::getFrequency).reversed())
            .limit(MAX_LIMIT)
            .toList();
    }

    // todo: 테스트 코드 작성 시 구현
    @Override
    public Optional<AutoCompleteKeyword> findByKeyword(String keyword) {
        return Optional.empty();
    }

    // todo: 테스트 코드 작성 시 구현
    @Override
    public AutoCompleteKeyword save(AutoCompleteKeyword autoCompleteKeyword) {
        return null;
    }


    public void saveAll(List<AutoCompleteKeyword> autoCompleteKeywords) {
        data.addAll(autoCompleteKeywords);
    }

}
