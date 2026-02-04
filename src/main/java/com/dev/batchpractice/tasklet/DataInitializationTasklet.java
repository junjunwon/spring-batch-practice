package com.dev.batchpractice.tasklet;

import com.dev.batchpractice.entity.BatchInput;
import com.dev.batchpractice.repository.BatchInputRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializationTasklet implements Tasklet {

	private final BatchInputRepository batchInputRepository;
	private static final int TOTAL_COUNT = 100;
	private static final int BATCH_SIZE = 10;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		long count = batchInputRepository.countByProcessedFalse();
		
		if (count == 0) {
			log.info("Starting to create {} test data...", TOTAL_COUNT);
			createTestData(TOTAL_COUNT);
			log.info("Finished creating {} test data.", TOTAL_COUNT);
		} else {
			log.info("Test data already exists. Count: {}", count);
		}
		
		return RepeatStatus.FINISHED;
	}

	private void createTestData(int totalCount) {
		List<BatchInput> batch = new ArrayList<>();

		for (int i = 1; i <= totalCount; i++) {
			BatchInput input = BatchInput.builder()
					.name("TestData-" + i)
					.data("Data-" + i + "-" + System.currentTimeMillis())
					.status(i % 10)
					.processed(false)
					.build();

			batch.add(input);

			if (batch.size() >= BATCH_SIZE) {
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
