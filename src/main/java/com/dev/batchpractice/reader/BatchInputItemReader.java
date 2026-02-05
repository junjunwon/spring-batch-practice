package com.dev.batchpractice.reader;

import com.dev.batchpractice.entity.BatchInput;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchInputItemReader {

    private final EntityManagerFactory entityManagerFactory;
    private static final int CHUNK_SIZE = 10;

    @Bean
    @StepScope
    public JpaPagingItemReader<BatchInput> batchInputReader() {
        return new JpaPagingItemReaderBuilder<BatchInput>()
                .name("batchInputReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT b FROM BatchInput b WHERE b.processed = false ORDER BY b.id")
                .pageSize(CHUNK_SIZE)
                .build();
    }
}
