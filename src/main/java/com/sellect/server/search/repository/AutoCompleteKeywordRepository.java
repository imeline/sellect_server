package com.sellect.server.search.repository;

import com.sellect.server.search.domain.AutoCompleteKeyword;
import java.util.List;
import java.util.Optional;

public interface AutoCompleteKeywordRepository {

    List<AutoCompleteKeyword> findTopKeywordsStartingWith(String query);

    Optional<AutoCompleteKeyword> findByKeyword(String keyword);

    AutoCompleteKeyword save(AutoCompleteKeyword autoCompleteKeyword);

}
