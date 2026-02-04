package com.dev.batchpractice.config;

import com.dev.batchpractice.tasklet.SimpleJobTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	private final SimpleJobTasklet simpleJobTasklet;

	@Bean
	public Job stepNextJob() {
		return new JobBuilder("stepNextJob", jobRepository)
				.start(step1())
					.on("FAILED")
					.to(jobFailStep()) // step1이 실패할 경우 jobFailStep으로 이동
				.from(step1()) // step1로부터
					.on("*") // FAILED 외에 모든 경우
					.to(finalStep()) // finalStep으로 이동
				.end() // Job 종료
				.build();
	}

	@Bean
	public Step step1() {
		return new StepBuilder("simpleStep1", jobRepository)
				.tasklet(simpleJobTasklet, transactionManager)
				.build();
	}

	@Bean
	public Step finalStep() {
		return new StepBuilder("finalStep", jobRepository)
				.tasklet((contribution, chunkContext) -> {
					System.out.println(">> This is finalStep");
					return RepeatStatus.FINISHED;
				})
				.build();
	}

	@Bean
	public Step jobFailStep() {
		return new StepBuilder("JobFailStep", jobRepository)
				.tasklet((contribution, chunkContext) -> {
					log.info(">>>>> This is stepNextJob jobFailStep");
					return RepeatStatus.FINISHED;
				})
				.build();
	}
}
