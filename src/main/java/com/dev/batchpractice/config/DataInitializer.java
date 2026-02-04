package com.dev.batchpractice.config;

import com.dev.batchpractice.entity.BatchInput;
import com.dev.batchpractice.repository.BatchInputRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer {

	private final BatchInputRepository batchInputRepository;

	@PostConstruct
	public void initData() {
		long count = batchInputRepository.countByProcessedFalse();
		if (count == 0) {
			log.info("Starting to create 100,000 test data...");
			createTestData(100000);
			log.info("Finished creating 100,000 test data.");
		} else {
			log.info("Test data already exists. Count: {}", count);
		}
	}

	private void createTestData(int totalCount) {
		int batchSize = 1000;
		List<BatchInput> batch = new ArrayList<>();

		for (int i = 1; i <= totalCount; i++) {
			BatchInput input = BatchInput.builder()
					.name("TestData-" + i)
					.data("Data-" + i + "-" + System.currentTimeMillis())
					.status(i % 10)
					.processed(false)
					.build();

			batch.add(input);

			if (batch.size() >= batchSize) {
				batchInputRepository.saveAll(batch);
				batch.clear();
				if (i % 10000 == 0) {
					log.info("Created {} test data...", i);
				}
			}
		}

		if (!batch.isEmpty()) {
			batchInputRepository.saveAll(batch);
		}
	}
}
