package com.dev.batchpractice.config;

import com.dev.batchpractice.tasklet.SimpleJobTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchSimpleConfig {

	private final JobRepository jobRepository;

	@Bean
	public Job simpleJob() {
		return new JobBuilder("simpleJob", jobRepository)
				.start(simpleStep1())
				.build();
	}

	@Bean
	public Step simpleStep1() {
		return new StepBuilder("simpleStep1", jobRepository)
				.tasklet((contribution, chunkContext) -> {
					System.out.println(">> This is Step 1");
					return RepeatStatus.FINISHED;
				})
				.build();
	}
}
