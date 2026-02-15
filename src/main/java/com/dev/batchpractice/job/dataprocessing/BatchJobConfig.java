package com.dev.batchpractice.job.dataprocessing;

import com.dev.batchpractice.domain.entity.BatchInput;
import com.dev.batchpractice.domain.entity.BatchOutput;
import com.dev.batchpractice.common.listener.BatchPerformanceListener;
import com.dev.batchpractice.job.dataprocessing.processor.ApiCallItemProcessor;
import com.dev.batchpractice.job.dataprocessing.tasklet.DataInitializationTasklet;
import com.dev.batchpractice.job.dataprocessing.tasklet.FailStepTasklet;
import com.dev.batchpractice.job.dataprocessing.writer.BatchOutputWriter;
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

	private final BatchPerformanceListener batchPerformanceListener;

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
				.listener(batchPerformanceListener)
				.build();
	}

	@Bean
	public Step dataInitializationStep() {
		return new StepBuilder("dataInitializationStep", jobRepository)
				.tasklet(dataInitializationTasklet, transactionManager)
				.listener(batchPerformanceListener)
				.build();
	}

	@Bean
	public Step dataProcessingStep(@Qualifier("batchInputReader") JpaPagingItemReader<BatchInput> batchInputReader) {
		return new StepBuilder("dataProcessingStep", jobRepository)
				.<BatchInput, BatchOutput>chunk(CHUNK_SIZE)
				.reader(batchInputReader)
				.processor(apiCallItemProcessor)
				.writer(batchOutputWriter)
				.listener(batchPerformanceListener)
				.build();
	}

	@Bean
	public Step failStep() {
		return new StepBuilder("failStep", jobRepository)
				.tasklet(failStepTasklet, transactionManager)
				.listener(batchPerformanceListener)
				.build();
	}
}
