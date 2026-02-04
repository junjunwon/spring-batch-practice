package com.dev.batchpractice.reader;

import com.dev.batchpractice.entity.BatchInput;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStream;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchInputReader implements ItemReader<BatchInput>, ItemStream {

	private final EntityManagerFactory entityManagerFactory;
	private static final int PAGE_SIZE = 1000;
	
	private JpaPagingItemReader<BatchInput> delegate;

	@PostConstruct
	private void initReader() {
		delegate = new JpaPagingItemReaderBuilder<BatchInput>()
				.name("batchInputReader")
				.entityManagerFactory(entityManagerFactory)
				.queryString("SELECT b FROM BatchInput b WHERE b.processed = false ORDER BY b.id")
				.pageSize(PAGE_SIZE)
				.build();
		
		// EntityManager 초기화를 위해 afterPropertiesSet 호출
		try {
			delegate.afterPropertiesSet();
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize JpaPagingItemReader", e);
		}
	}

	@Override
	public BatchInput read() throws Exception {
		return delegate.read();
	}

	@Override
	public void open(ExecutionContext executionContext) {
		if (delegate instanceof ItemStream) {
			((ItemStream) delegate).open(executionContext);
		}
	}

	@Override
	public void update(ExecutionContext executionContext) {
		if (delegate instanceof ItemStream) {
			((ItemStream) delegate).update(executionContext);
		}
	}

	@Override
	public void close() {
		if (delegate instanceof ItemStream) {
			((ItemStream) delegate).close();
		}
	}
}
