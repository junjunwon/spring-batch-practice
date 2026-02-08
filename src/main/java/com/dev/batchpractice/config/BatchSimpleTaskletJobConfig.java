package com.dev.batchpractice.config;

import com.dev.batchpractice.tasklet.SimpleJobTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BatchSimpleTaskletJobConfig {

	private final JobRepository jobRepository;
	private final SimpleJobTasklet simpleJobTasklet;

	@Bean
	public Job simpleTaskletJob() {
		return new JobBuilder("simpleTaskletJob", jobRepository)
				.start(simpleTaskletStep())
				.build();
	}

	@Bean
	public Step simpleTaskletStep() {
		return new StepBuilder("simpleStep", jobRepository)
				.tasklet(simpleJobTasklet)
				.build();
	}
}
