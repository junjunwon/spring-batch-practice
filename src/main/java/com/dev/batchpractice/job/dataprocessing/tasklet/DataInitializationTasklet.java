package com.dev.batchpractice.job.dataprocessing.tasklet;

import com.dev.batchpractice.domain.entity.BatchInput;
import com.dev.batchpractice.domain.repository.BatchInputRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
		long totalCount = batchInputRepository.count();

		if (totalCount == 0) {
			log.info("BatchInput table is empty. Creating {} test data...", TOTAL_COUNT);
			createTestData();
			log.info("Finished creating {} test data.", TOTAL_COUNT);
		} else {
			log.info("BatchInput table already has data. Total count: {}, skipping creation.", totalCount);
		}

		return RepeatStatus.FINISHED;
	}

	private void createTestData() {
		List<BatchInput> batch = new ArrayList<>(BATCH_SIZE);

		for (int i = 1; i <= DataInitializationTasklet.TOTAL_COUNT; i++) {
			BatchInput input = BatchInput.builder()
					.name("TestData-" + i)
					.data("Data-" + i + "-" + System.currentTimeMillis())
					.status(i % BATCH_SIZE)
					.processed(false)
					.build();

			batch.add(input);

			if (batch.size() >= BATCH_SIZE) {
				batchInputRepository.saveAll(batch);
				batch.clear();
				log.info("Created {} / {} test data...", i, DataInitializationTasklet.TOTAL_COUNT);
			}
		}

		if (!batch.isEmpty()) {
			batchInputRepository.saveAll(batch);
			log.info("Created {} / {} test data...", DataInitializationTasklet.TOTAL_COUNT, DataInitializationTasklet.TOTAL_COUNT);
		}
	}
}
