package com.dev.batchpractice.config;

import com.dev.batchpractice.entity.BatchInput;
import com.dev.batchpractice.entity.BatchOutput;
import com.dev.batchpractice.processor.ApiCallItemProcessor;
import com.dev.batchpractice.tasklet.DataInitializationTasklet;
import com.dev.batchpractice.tasklet.FailStepTasklet;
import com.dev.batchpractice.writer.BatchOutputWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchJobConfig {
	private static final int CHUNK_SIZE = 10;

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	private final DataInitializationTasklet dataInitializationTasklet;
	private final FailStepTasklet failStepTasklet;

	private final ApiCallItemProcessor apiCallItemProcessor;
	private final BatchOutputWriter batchOutputWriter;

	@Bean
	public Job dataProcessingJob(Step dataInitializationStep, Step dataProcessingStep, Step failStep) {
		return new JobBuilder("dataProcessingJob", jobRepository)
				.start(dataInitializationStep)
					.on("FAILED")
					.to(failStep)
				.from(dataInitializationStep)
					.on("*")
					.to(dataProcessingStep)
						.on("FAILED")
						.to(failStep)
					.from(dataProcessingStep)
						.on("*")
						.end()
				.end()
				.build();
	}

	@Bean
	public Step dataInitializationStep() {
		return new StepBuilder("dataInitializationStep", jobRepository)
				.tasklet(dataInitializationTasklet, transactionManager)
				.build();
	}

	@Bean
	public Step dataProcessingStep(@Qualifier("batchInputReader") JpaPagingItemReader<BatchInput> batchInputReader) {
		return new StepBuilder("dataProcessingStep", jobRepository)
				.<BatchInput, BatchOutput>chunk(CHUNK_SIZE)
				.reader(batchInputReader)
				.processor(apiCallItemProcessor)
				.writer(batchOutputWriter)
				.build();
	}

	@Bean
	public Step failStep() {
		return new StepBuilder("failStep", jobRepository)
				.tasklet(failStepTasklet, transactionManager)
				.build();
	}
}
