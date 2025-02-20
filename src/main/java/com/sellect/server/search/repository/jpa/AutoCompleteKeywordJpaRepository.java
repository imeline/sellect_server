package com.sellect.server.search.repository.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AutoCompleteKeywordJpaRepository extends JpaRepository<AutoCompleteKeywordEntity, Long> {

    @Query("SELECT s FROM AutoCompleteKeywordEntity s "
        + "WHERE s.keyword LIKE :prefix%")
    List<AutoCompleteKeywordEntity> findTopByKeywordStartingWith(
        @Param("prefix") String prefix,
        Pageable pageable
    );

    Optional<AutoCompleteKeywordEntity> findByKeywordAndDeleteAtIsNull(String keyword);

    Optional<AutoCompleteKeywordEntity> findByKeyword(String keyword);
}
