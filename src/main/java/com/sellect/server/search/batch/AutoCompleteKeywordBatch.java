package com.sellect.server.search.batch;

import com.sellect.server.search.repository.SearchLogEntity;
import com.sellect.server.search.repository.jpa.AutoCompleteKeywordEntity;
import com.sellect.server.search.repository.jpa.AutoCompleteKeywordJpaRepository;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class AutoCompleteKeywordBatch {

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public Job processAutoCompleteKeywordJob(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaPagingItemReader<SearchLogEntity> searchLogItemReader) {

        return new JobBuilder("processAutoCompleteKeywordJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(processAutoCompleteKeywordStep(jobRepository, transactionManager,
                searchLogItemReader))
            .build();
    }

    @Bean
    @JobScope
    public Step processAutoCompleteKeywordStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaPagingItemReader<SearchLogEntity> searchLogItemReader) {

        return new StepBuilder("processAutoCompleteKeywordStep", jobRepository)
            .<SearchLogEntity, SearchLogEntity>chunk(100, transactionManager)
            .reader(searchLogItemReader)
            .build();
    }

    @Bean
    public JpaPagingItemReader<SearchLogEntity> searchLogItemReader(
        EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReader<>() {
            {
                setEntityManagerFactory(entityManagerFactory);
                setQueryString("SELECT s FROM SearchLogEntity s "
                    + "WHERE s.timestamp BETWEEN :startDate AND :endDate "
                    + "AND s.filterApplied = false "
                    + "AND s.resultCount > 0 "
                    + "GROUP BY s.sessionId, s.searchKeyword");
                setParameterValues(Map.of(
                    "startDate", LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN),
                    "endDate", LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX)
                ));
                setPageSize(100);
            }
        };
    }

    @Bean
    public ItemProcessor<SearchLogEntity, AutoCompleteKeywordEntity> autoCompleteKeywordProcessor(
        AutoCompleteKeywordJpaRepository autoCompleteKeywordRepository) {

        return item -> {
            AutoCompleteKeywordEntity autoCompleteKeyword = autoCompleteKeywordRepository.findByKeyword(
                    item.getSearchKeyword())
                .orElse(null);

            if (autoCompleteKeyword == null) {
                autoCompleteKeyword = autoCompleteKeywordRepository.save(
                    AutoCompleteKeywordEntity.builder()
                        .keyword(item.getSearchKeyword())
                        .frequency(1L)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
            } else {
                autoCompleteKeyword.incrementFrequency();
            }
            return autoCompleteKeyword;
        };
    }

    @Bean
    public JpaItemWriter<AutoCompleteKeywordEntity> autoCompleteKeywordWriter(
        EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<AutoCompleteKeywordEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
