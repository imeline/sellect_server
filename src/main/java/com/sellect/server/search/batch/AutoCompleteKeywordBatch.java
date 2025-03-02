package com.sellect.server.search.batch;

import com.sellect.server.search.repository.SearchLogEntity;
import com.sellect.server.search.repository.jpa.AutoCompleteKeywordEntity;
import com.sellect.server.search.repository.jpa.AutoCompleteKeywordJpaRepository;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
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
        Step processAutoCompleteKeywordStep) {

        return new JobBuilder("processAutoCompleteKeywordJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(processAutoCompleteKeywordStep)
            .build();
    }

    @Bean
    @JobScope
    public Step processAutoCompleteKeywordStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaPagingItemReader<SearchLogEntity> searchLogItemReader,
        ItemProcessor<SearchLogEntity, AutoCompleteKeywordEntity> autoCompleteKeywordProcessor,
        JpaItemWriter<AutoCompleteKeywordEntity> autoCompleteKeywordWriter) {

        return new StepBuilder("processAutoCompleteKeywordStep", jobRepository)
            .<SearchLogEntity, AutoCompleteKeywordEntity>chunk(100, transactionManager)
            .reader(searchLogItemReader)
            .processor(autoCompleteKeywordProcessor)
            .writer(autoCompleteKeywordWriter)
            .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<SearchLogEntity> searchLogItemReader(
        EntityManagerFactory entityManagerFactory,
        @Value("#{jobParameters['startDate']}") String startDateStr,
        @Value("#{jobParameters['endDate']}") String endDateStr) {

        LocalDateTime startDate = LocalDateTime.parse(startDateStr);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr);

        JpaPagingItemReader<SearchLogEntity> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT DISTINCT s FROM SearchLogEntity s "
            + "WHERE s.timestamp BETWEEN :startDate AND :endDate " // 하룻동안의 검색어 로그만 처리
            + "AND s.filterApplied = false " // 필터 적용된 검색어는 제외
            + "AND s.resultCount > 0 "); // 검색 결과가 있는 검색어만 처리
        reader.setParameterValues(Map.of("startDate", startDate, "endDate", endDate));
        reader.setPageSize(100);

        return reader;
    }

    @Bean
    @StepScope
    public ItemProcessor<SearchLogEntity, AutoCompleteKeywordEntity> autoCompleteKeywordProcessor(
        AutoCompleteKeywordJpaRepository autoCompleteKeywordRepository) {

        return item -> {
            AutoCompleteKeywordEntity autoCompleteKeyword = autoCompleteKeywordRepository.findByKeyword(
                    item.getKeyword())
                .orElse(null);

            if (autoCompleteKeyword == null) {
                autoCompleteKeyword = AutoCompleteKeywordEntity.builder()
                    .keyword(item.getKeyword())
                    .frequency(1L)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            } else {
                autoCompleteKeyword.incrementFrequency();
            }
            return autoCompleteKeyword;
        };
    }


    @Bean
    @StepScope
    public JpaItemWriter<AutoCompleteKeywordEntity> autoCompleteKeywordWriter(
        EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<AutoCompleteKeywordEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
